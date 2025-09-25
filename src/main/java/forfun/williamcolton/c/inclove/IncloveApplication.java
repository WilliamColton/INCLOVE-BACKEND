package forfun.williamcolton.c.inclove;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("forfun.williamcolton.c.inclove.mapper")
public class IncloveApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncloveApplication.class, args);
    }

}
