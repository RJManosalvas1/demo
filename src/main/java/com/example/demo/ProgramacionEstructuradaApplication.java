package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  // 👈 ESTA LÍNEA HABILITA EL CACHÉ EN TODO EL PROYECTO
public class ProgramacionEstructuradaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgramacionEstructuradaApplication.class, args);
	}
}
