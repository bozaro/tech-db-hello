package ru.mail.park.db.hello;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class ApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String SAMPLE_TASK = "{\"id\": 0,\"description\": \"string\",\"completed\": true}";
	private static final String SAMPLE_LIST = "[" + SAMPLE_TASK + "]";

	private long postTask() throws Exception {
		IdMatcherGetter idGetter = new IdMatcherGetter();
		mockMvc.perform(post("/api")
				.contentType(MediaType.APPLICATION_JSON)
				.content(SAMPLE_LIST))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(idGetter))
				.andExpect(jsonPath("$[0].description").value("string"))
				.andExpect(jsonPath("$[0].completed").value(true));

		return idGetter.id;
	}

	@Test
	public void testList() throws Exception {
		long id = postTask();

		mockMvc.perform(get("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(id))
				.andExpect(jsonPath("$[0].description").value("string"))
				.andExpect(jsonPath("$[0].completed").value(true));
	}

	@Test
	public void testGet() throws Exception {

		long id = postTask();

		mockMvc.perform(get("/api/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.description").value("string"))
				.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	public void testGetNotFound() throws Exception {
		mockMvc.perform(get("/api/42"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testDelete() throws Exception {

		long id = postTask();

		mockMvc.perform(delete("/api/" + id))
				.andExpect(status().isNoContent());
	}

	@Test
	public void testDeleteNotFound() throws Exception {
		mockMvc.perform(delete("/api/42"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testUpdate() throws Exception {
		long id = postTask();
		mockMvc.perform(put("/api/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\": -42,\"description\": \"foobar\",\"completed\": false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.description").value("foobar"))
				.andExpect(jsonPath("$.completed").value(false));
	}

	@Test
	public void testUpdateNotFound() throws Exception {
		mockMvc.perform(put("/api/42")
				.contentType(MediaType.APPLICATION_JSON)
				.content(SAMPLE_TASK))
				.andExpect(status().isNotFound());
	}

	private static class IdMatcherGetter extends BaseMatcher<Number> {
		private long id;

		@Override
		public boolean matches(Object item) {
			id = ((Number) item).longValue();
			return true;
		}

		@Override
		public void describeTo(Description description) {
			// no code
		}
	}

}
