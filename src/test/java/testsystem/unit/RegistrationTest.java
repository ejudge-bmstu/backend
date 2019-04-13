package testsystem.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import testsystem.domain.Profile;
import testsystem.domain.User;
import testsystem.domain.UserRole;
import testsystem.dto.UserDTO;
import testsystem.exception.EmailAlreadyExistsException;
import testsystem.exception.UserAlreadyExistsException;
import testsystem.repository.ProfileRepository;
import testsystem.repository.UserRepository;
import testsystem.service.UserServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationTest {

    @Mock
    private UserRepository repositoryMock;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Profile profile;
    private UserDTO userDTO;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Before
    public void init() {
        String password = encoder.encode("password");
        Mockito.when(passwordEncoder.encode("password")).thenReturn((password));
        user = new User("username", "email", password);
        profile = new Profile();
    }

    @Test
    public void createUserIfNotExist() {
        userDTO = UserDTO.user("username", "email", "password");

        Mockito.when(repositoryMock.save(Mockito.argThat(argument -> {
            Assert.assertEquals(user.getUsername(), argument.getUsername());
            Assert.assertEquals(user.getEmail(), argument.getEmail());
            Assert.assertEquals(user.getPassword_hash(), argument.getPassword_hash());
            Assert.assertEquals(UserRole.user, argument.getRole());
            Assert.assertFalse(argument.isEnabled());
            return true;
        }))).thenReturn(user);

        Mockito.when(profileRepository.save(Mockito.argThat(argument -> {
            Assert.assertEquals(profile.getName(), argument.getName());
            Assert.assertEquals(profile.getBirthday(), argument.getBirthday());
            Assert.assertEquals(profile.getSex(), argument.getSex());
            Assert.assertEquals(profile.getSurname(), argument.getSurname());
            return true;
        }))).thenReturn(profile);

        Mockito.when(repositoryMock.findByUsername("username")).thenReturn(null);
        Mockito.when(repositoryMock.findByEmail("email")).thenReturn(null);

        user.setProfile(profile);

        User newUser = userService.registerUser(userDTO);

        Assert.assertEquals(user, newUser);

        Mockito.verify(repositoryMock, Mockito.times(1)).findByUsername(userDTO.getUsername());
        Mockito.verify(repositoryMock, Mockito.times(1)).findByEmail(userDTO.getEmail());
        Mockito.verify(repositoryMock, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(profileRepository, Mockito.times(1)).save(Mockito.any(Profile.class));
        Mockito.verifyNoMoreInteractions(repositoryMock);
        Mockito.verifyNoMoreInteractions(profileRepository);
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void createUserIfExist() {
        userDTO = UserDTO.user("username", "email", "password");
        Mockito.when(repositoryMock.findByUsername("username")).thenReturn(user);

        userService.registerUser(userDTO);
    }

    @Test(expected = EmailAlreadyExistsException.class)
    public void createUserIfEmailExist() {
        userDTO = UserDTO.user("username", "email", "password");
        Mockito.when(repositoryMock.findByUsername("username")).thenReturn(null);
        Mockito.when(repositoryMock.findByEmail("email")).thenReturn(user);

        userService.registerUser(userDTO);
    }

}
