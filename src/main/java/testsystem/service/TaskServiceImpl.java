package testsystem.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import testsystem.domain.*;
import testsystem.dto.*;
import testsystem.exception.*;
import testsystem.exception.zip.*;
import testsystem.repository.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LimitRepository limitRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ExampleRepository exampleRepository;

    @Autowired
    private UserSolutionRepository userSolutionRepository;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private TestsystemService testsystemService;

    public void setTestsystemService(TestsystemService service) {
        testsystemService = service;
    }

    @Override
    public TaskListDTO getTasksList(String id, int page, int limit, boolean categorized) {
        Category category = null;

        if (categorized) {
            UUID uuid = validateId(id);
            category = categoryService.validateCategoryExists(uuid);
        }

        Pageable pageableRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<Task> tasks = taskRepository.findByCategory(category, pageableRequest);

        List<TaskDTO> list = new ArrayList<>();
        tasks.forEach(task -> list.add(
                new TaskDTO(task.getId().toString(), task.getName())
        ));

        return new TaskListDTO(tasks.getTotalPages(), category != null ? category.getName() : null, list);
    }

    @Override
    public TaskDescriptionDTO getTask(String id) {
        UUID uuid = validateId(id);
        Task task = validateTaskExists(uuid);

        String name = task.getName();
        String description = task.getDescription();
        String access = task.getReport_permission();
        CategoryDTO category = getCategoryDTO(task);
        List<LanguageDTO> languages = getTotalLanguages();
        List<LimitDTO> limits = getLimitsDTO(task);
        List<ExampleDTO> examples = getExamplesDTO(task);

        return new TaskDescriptionDTO(name, description, access, category, languages, limits, examples);
    }

    @Override
    public Task addTask(TaskNewDTO taskDTO, String[] inputs, String[] outputs, MultipartFile file) {
        String name = taskDTO.getName();
        String description = taskDTO.getDescription();
        String access = taskDTO.getAccess_report();

        UUID uuid = categoryService.validateId(taskDTO.getCategory());
        Category category = categoryService.validateCategoryExists(uuid);

        validateTaskNameExists(name, category);

        Task task = new Task(name, description, access, category);

        List<Test> tests = unzipFileAndGetTests(file);
        Task saved = taskRepository.save(task);

        saveTests(saved, tests);
        saveLimits(saved, taskDTO);
        saveExamples(saved, inputs, outputs);

        return taskRepository.findById(saved.getId()).get();
    }

    @Override
    public void addSolution(TaskDTO taskDTO, MultipartFile multipartFile) {

        User currentUser = userService.getCurrentUser();

        Long date = System.currentTimeMillis();

        UUID uuid = validateId(taskDTO.getId());
        Task task = validateTaskExists(uuid);

        Answer answer = getAnswer(multipartFile);

        Status status = new Status();

        UserSolution solution = new UserSolution(date, currentUser, task, answer, status);

        UserSolution saved = userSolutionRepository.save(solution);

        try {
            if (testsystemService.sendRequestToTestingServer(saved.getId().toString()) != 200) {
                userSolutionRepository.delete(saved);
            }
        } catch (IOException e) {
            userSolutionRepository.delete(saved);
            throw new TestsystemRequestException();
        }
    }

    @Override
    public List<ResultDTO> getResults(String id) {
        Task task = validateTaskExists(validateId(id));

        List<ResultDTO> results = new ArrayList<>();

        if (task.getSolutions() != null) {
            task.getSolutions().forEach(userSolution -> {
                User user = userSolution.getUser();
                results.add(new ResultDTO(
                        task.getName(),
                        task.getId().toString(),
                        user.getUsername(),
                        user.getId().toString(),
                        task.getTests().size(),
                        userSolution.getStatus().getPassed(),
                        userSolution.getStatus().getResult(),
                        userSolution.getStatus().getExtended_information(),
                        userSolution.getSolution_date()));
            });
        }

        return results;
    }

    private void saveTests(Task task, List<Test> tests) {
        tests.forEach(test -> {
            test.setTask(task);
            testRepository.save(test);
        });
    }

    private void saveLimits(Task task, TaskNewDTO taskDTO) {
        Limit limitC = new Limit(
                taskDTO.getMemory_limit_c(), taskDTO.getTime_limit_c(), task, ProgrammingLanguage.c
        );
        Limit limitCpp = new Limit(
                taskDTO.getMemory_limit_cpp(), taskDTO.getTime_limit_cpp(), task, ProgrammingLanguage.cpp
        );
        Limit limitPython = new Limit(
                taskDTO.getMemory_limit_python(), taskDTO.getTime_limit_python(), task, ProgrammingLanguage.python
        );

        limitRepository.save(limitC);
        limitRepository.save(limitCpp);
        limitRepository.save(limitPython);
    }

    private void saveExamples(Task task, String[] inputs, String[] outputs) {
        if (inputs.length != outputs.length)
            throw new ExampleException();
        int size = inputs.length;
        for (int i = 0; i < size; i++) {
            Example example = new Example(inputs[i], outputs[i], task);
            exampleRepository.save(example);
        }
    }

    private List<Test> unzipFileAndGetTests(MultipartFile multipartFile) {
        File file = null;
        List<Test> testsFromFile;
        try {
            file = getFileFromMultipartFile(multipartFile);
            testsFromFile = getTestsFromFile(file);
        } catch (IOException e) {
            throw new TestFileIOException();
        } finally {
            if (file != null && file.getName().endsWith("tmp"))
                file.delete();
        }
        return testsFromFile;
    }

    private List<Test> getTestsFromFile(File file) throws IOException {
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new OpenFileException();
        }

        Map<String, Test> map = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                map.put(entry.getName().substring(0, entry.getName().lastIndexOf("/")), new Test());
            } else {
                String[] dirNameVsDataName = entry.getName().split("/");
                if (dirNameVsDataName.length < 2)
                    throw new InvalidFileInZip();
                if (dirNameVsDataName.length > 2)
                    throw new InvalidFileInDirectory(dirNameVsDataName[0]);
                if (!dirNameVsDataName[1].matches("in.txt|out.txt"))
                    throw new InvalidFileInDirectory(dirNameVsDataName[0]);

                String data = readFile(zipFile, entry);

                if (dirNameVsDataName[1].equals("in.txt"))
                    map.get(dirNameVsDataName[0]).setInput_data(data);
                else
                    map.get(dirNameVsDataName[0]).setOutput_data(data);
            }
        }

        map.forEach((key, test) -> {
            if (test.getInput_data() == null)
                throw new NoInputData(key);
            if (test.getOutput_data() == null)
                throw new NoOutputData(key);
        });

        if (map.isEmpty())
            throw new NoOneTestException();

        return new ArrayList<>(map.values());
    }

    private UUID validateId(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NoSuchTaskException();
        }
        return uuid;
    }

    private Task validateTaskExists(UUID id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent())
            throw new NoSuchTaskException();
        return taskOptional.get();
    }

    private void validateTaskNameExists(String name, Category category) {
        if (taskRepository.findByCategory(category).stream().anyMatch(task -> task.getName().equals(name)))
            throw new TaskAlreadyExistsException(name, category.getName());
    }

    private CategoryDTO getCategoryDTO(Task task) {
        CategoryDTO categoryDTO = null;
        Category category = task.getCategory();
        if (category != null)
            categoryDTO = new CategoryDTO(category.getId().toString(), category.getName(), category.getTasks().size());
        return categoryDTO;
    }

    private List<LimitDTO> getLimitsDTO(Task task) {
        List<LimitDTO> limits = new ArrayList<>();
        if (task.getLimits() != null) {
            task.getLimits().forEach(limit ->
                limits.add(
                        new LimitDTO(limit.getProgramming_language().toString(), limit.getMemory_limit(), limit.getTime_limit())
                )
            );
        }
        return limits;
    }

    private List<ExampleDTO> getExamplesDTO(Task task) {
        List<ExampleDTO> examples = new ArrayList<>();
        if (task.getExamples() != null) {
            task.getExamples().forEach(example ->
                    examples.add(
                            new ExampleDTO(example.getInput_data(), example.getOutput_data())
                    )
            );
        }
        return examples;
    }

    private List<LanguageDTO> getTotalLanguages() {
        List<LanguageDTO> languages = new ArrayList<>();
        for (ProgrammingLanguage value : ProgrammingLanguage.values())
            languages.add(new LanguageDTO(value.toString()));
        return languages;
    }

    private File getFileFromMultipartFile(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), "tmp");
        FileOutputStream outputStream = new FileOutputStream(file);
        IOUtils.copy(multipartFile.getInputStream(), outputStream);
        outputStream.close();

        return file;
    }

    private String readFile(ZipFile zipFile, ZipEntry entry) throws IOException {

        InputStream is = zipFile.getInputStream(entry);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (is, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    private Answer getAnswer(MultipartFile multipartFile) {
        String extension = getExtension(multipartFile.getOriginalFilename());
        ProgrammingLanguage language = getLanguageFromExtension(extension);

        String text;
        try {
            text = new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SolutionFileIOError();
        }

        return new Answer(text, language);
    }

    private String getExtension(String name) {
        if (name == null)
            throw new UnknownLanguageException();
        String[] split = name.split("\\.");
        if (split.length < 2)
            throw new UnknownLanguageException();
        return split[split.length-1];
    }

    private ProgrammingLanguage getLanguageFromExtension(String ext) {
        if (ext.equals("cpp"))
            return ProgrammingLanguage.cpp;
        if (ext.equals("c"))
            return ProgrammingLanguage.c;
        if (ext.equals("py"))
            return ProgrammingLanguage.python;
        throw new UnknownLanguageException();
    }
}
