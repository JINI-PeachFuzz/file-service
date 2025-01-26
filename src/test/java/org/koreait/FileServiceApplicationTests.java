package org.koreait;

import org.junit.jupiter.api.Test;
import org.koreait.file.constants.FileStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"default","test"})
class FileServiceApplicationTests {

	@Test
	void contextLoads() {
		System.out.println(FileStatus.ALL.ordinal());
	}

}
