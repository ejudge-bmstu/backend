package testsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import testsystem.domain.Category;
import testsystem.domain.Task;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByCategory(Category category);
    Page<Task> findByCategory(Category category, Pageable pageable);
}
