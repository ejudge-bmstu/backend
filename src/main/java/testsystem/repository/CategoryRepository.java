package testsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import testsystem.domain.Category;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Category findByName(String name);

}
