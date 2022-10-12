package com.springWebExample1.springWebExample1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.springframework.util.StreamUtils.BUFFER_SIZE;


@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringWebExample1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringWebExample1Application.class, args);
	}

	@Bean
	@ConfigurationProperties(prefix = "droid")
	Droid createDroid(){
		return new Droid();
	}

}

interface CoffeeRepository extends CrudRepository<Coffee, String> {

}
@Entity
class Coffee {
	@Id
	private String id;
	private String name;

	public Coffee(String id, String name){
		this.id = id;
		this.name = name;
	}

	public Coffee(String name){
		this.id = UUID.randomUUID().toString();
		this.name = name;
	}

	public Coffee(){

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

@Component
class DataLoader {
	private final CoffeeRepository coffeeRepository;

	public DataLoader(CoffeeRepository coffeeRepository){
		this.coffeeRepository = coffeeRepository;
	}

	@PostConstruct
	private void loadData(){
		coffeeRepository.saveAll(List.of(
				new Coffee("Cafe Cereza"),
				new Coffee("Cafe Ganador"),
				new Coffee("Cafe Lareno"),
				new Coffee("Cafe Tres Pontas")
		));
	}
}

@RestController
@RequestMapping(value = "/")
class RestApiDemoController {

	private final CoffeeRepository coffeeRepository;

	public RestApiDemoController(CoffeeRepository coffeeRepository){
		this.coffeeRepository = coffeeRepository;
	}

	@GetMapping("/coffees")
	Iterable<Coffee> getCoffees(){
		return coffeeRepository.findAll();
	}

	@GetMapping("/coffees/{id}")
	Optional<Coffee> getCoffeeById(@PathVariable String id){
		return coffeeRepository.findById(id);
	}

	@GetMapping("/license")
	boolean getLicense()  {

		String urlToGetFrom = "https://dom.gosuslugi.ru/filestore/publicDownloadAllFilesServlet?context=licenses&uids=2b71a7fe-36d5-412f-8227-7c7c75f0cb73&zipFileName=%D0%A0%D0%B5%D0%B5%D1%81%D1%82%D1%80%20%D0%BB%D0%B8%D1%86%D0%B5%D0%BD%D0%B7%D0%B8%D0%B9%20%D1%81%D1%83%D0%B1%D1%8A%D0%B5%D0%BA%D1%82%D0%B0%20%D0%A0%D0%A4%20%D0%A1%D0%B0%D0%BC%D0%B0%D1%80%D1%81%D0%BA%D0%B0%D1%8F%20%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C.zip"; // URL to get it from


//		try {
//			URL fetchWebsite = new URL(urlToGetFrom);
//
//			Path path = Paths.get("D:/Лицензии/Тек_реестр.zip");
//			try (InputStream inputStream = fetchWebsite.openStream()) {
//				Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		try (ZipInputStream zipIn = new ZipInputStream( new BufferedInputStream(new FileInputStream("D:/Лицензии/Тек_реестр.zip"), BUFFER_SIZE),
				Charset.forName("windows-1251"))) {

			ZipEntry zipEntry;
			while ((zipEntry = zipIn.getNextEntry()) != null) {

				if(zipEntry.getName().endsWith(".xlsx")){
					Files.copy(zipIn, Path.of("D:/Лицензии/Тек_реестр.xlsx"),StandardCopyOption.REPLACE_EXISTING);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}




		try {
			FileInputStream file = new FileInputStream(new File("D:/Лицензии/Тек_реестр.xlsx"));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);


			Iterator<Row> rowIterator = sheet.iterator();
			int rowCount = 5; // to skip the first row, which is a header


			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				rowCount++;

				//from 2nd row onwards, after skipping header
				if (rowCount > 5) {

					// For each row, iterate through each columns
					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();

//						if (cell.getColumnIndex() == 2) {

							System.out.println(cell.getStringCellValue());

//						} else {
//							throw new RuntimeException("Unexpected index");
//						}

					}
				}

			}


		}catch (IOException e) {
			e.printStackTrace();
		};








		return true;

	}

	@PostMapping("/coffees")
	Coffee postCoffee(@RequestBody Coffee coffee){
		return coffeeRepository.save(coffee);
	}

	@PutMapping("/coffees/{id}")
	ResponseEntity<Coffee> putCoffee(@PathVariable String id, @RequestBody Coffee coffee){
		return (!coffeeRepository.existsById(id)) ? new ResponseEntity<>(coffeeRepository.save(coffee), HttpStatus.CREATED) : new ResponseEntity<>(coffeeRepository.save(coffee), HttpStatus.OK);
	}

	@DeleteMapping("/coffees/{id}")
	void deleteCoffee(@PathVariable String id){
		coffeeRepository.deleteById(id);
	}

}

@ConfigurationProperties(prefix = "greeting")
class Greeting{
	private String name;
	private String coffee;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCoffee() {
		return coffee;
	}

	public void setCoffee(String coffee) {
		this.coffee = coffee;
	}
}

@RestController
@RequestMapping("/greeting")
class GreetingController{
	private final Greeting greeting;

	public GreetingController(Greeting greeting){
		this.greeting = greeting;
	}

	@GetMapping
	String getGreeting(){
		return greeting.getName();
	}

	@GetMapping("/coffee")
	String getNameAndCoffee(){
		return greeting.getCoffee();
	}
}

@RestController
@RequestMapping("/droid")
class DroidController{
	private final Droid droid;

	public DroidController(Droid droid){
		this.droid = droid;
	}

	@GetMapping
	Droid getDroid(){
		return droid;
	}
}