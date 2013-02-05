package ch.inftec.ju.testing.db.data.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.inftec.ju.testing.db.data.entity.TestingEntity;

/**
 * Spring data CrudRepository for the TestingEntity entity.
 * @author Martin
 *
 */
public interface TeamRepo extends CrudRepository<TestingEntity, Long> {
	/*
	 * EclipseLink has a problem when the query should be created dynamically. When
	 * we explicitly define the query, we don't have this problem.
	 * Hibernate seems to work in both cases.
	 */
	@Query("select t from Team t where t.name = ?1")
	TestingEntity getByName(String name);
}
