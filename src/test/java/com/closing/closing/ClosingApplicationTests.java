package com.closing.closing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("테스트용 PostgreSQL 연결 설정이 준비된 후 활성화합니다.")
class ClosingApplicationTests {

	@Test
	void contextLoads() {
		// 전체 Spring 컨텍스트가 정상적으로 생성되는지 확인하는 테스트다.
	}

}
