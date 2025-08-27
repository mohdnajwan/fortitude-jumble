package asia.fourtitude.interviewq.jumble.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collection;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import asia.fourtitude.interviewq.jumble.TestConfig;
import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.GameGuessInput;

@WebMvcTest(GameApiController.class)
@Import(TestConfig.class)
class GameApiControllerTest {

    static final ObjectMapper OM = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    JumbleEngine jumbleEngine;

    /*
     * NOTE: Refer to "RootControllerTest.java", "GameWebControllerTest.java"
     * as reference. Search internet for resource/tutorial/help in implementing
     * the unit tests.
     *
     * Refer to "http://localhost:8080/swagger-ui/index.html" for REST API
     * documentation and perform testing.
     *
     * Refer to Postman collection ("interviewq-jumble.postman_collection.json")
     * for REST API documentation and perform testing.
     */

    @Test
    void whenCreateNewGame_thenSuccess() throws Exception {
        /*
         * Doing HTTP GET "/api/game/new"
         *
         * Input: None
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Created new game."
         * c) `id` is not null
         * d) `originalWord` is not null
         * e) `scrambleWord` is not null
         * f) `totalWords` > 0
         * g) `remainingWords` > 0 and same as `totalWords`
         * h) `guessedWords` is empty list
         */

         MvcResult result = this.mvc.perform(get("/api/game/new"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.result").value("Created new game."))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.original_word").isNotEmpty())
            .andExpect(jsonPath("$.scramble_word").isNotEmpty())
            .andExpect(jsonPath("$.total_words").value(greaterThan(0)))
            .andExpect(jsonPath("$.remaining_words").value(greaterThan(0)))
            .andExpect(jsonPath("$.guessed_words").isArray())
            .andExpect(jsonPath("$.guessed_words").isEmpty())
            .andReturn();

            String json = result.getResponse().getContentAsString();
            assertEquals(OM.readTree(json).get("total_words").asInt(), OM.readTree(json).get("remaining_words").asInt()); // `remainingWords` > 0 and same as `totalWords`
    }

    @Test
    void givenMissingId_whenPlayGame_thenInvalidId() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Input: JSON request body
         * a) `id` is null or missing
         * b) `word` is null/anything or missing
         *
         * Expect: Assert these
         * a) HTTP status == 404
         * b) `result` equals "Invalid Game ID."
         */

        GameGuessInput input = new GameGuessInput();
        input.setId(null);

        String requestBody = OM.writeValueAsString(input);

        this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.result").value("Invalid Game ID."));

    }

    @Test
    void givenMissingRecord_whenPlayGame_thenRecordNotFound() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Input: JSON request body
         * a) `id` is some valid ID (but not exists in game system)
         * b) `word` is null/anything or missing
         *
         * Expect: Assert these
         * a) HTTP status == 404
         * b) `result` equals "Game board/state not found."
         */
        
        GameGuessInput input = new GameGuessInput();
        input.setId(UUID.randomUUID().toString());
        input.setWord(null);

        this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(OM.writeValueAsString(input)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.result").value("Game board/state not found."));
    }

    @Test
    void givenCreateNewGame_whenSubmitNullWord_thenGuessedIncorrectly() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is null or missing
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Guessed incorrectly."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` is equals to `input.word`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is equals to `remainingWords` of previous game state (no change)
         * i) `guessedWords` is empty list (because this is first attempt)
         */

        MvcResult newGameResult = this.mvc.perform(get("/api/game/new"))
            .andExpect(status().isOk())
            .andReturn();

        String newGameJson = newGameResult.getResponse().getContentAsString();
        String gameId = OM.readTree(newGameJson).get("id").asText();
        String originalWord = OM.readTree(newGameJson).get("original_word").asText();

        GameGuessInput input = new GameGuessInput();
        input.setId(gameId);
        input.setWord(null);

        String requestBody = OM.writeValueAsString(input);

        this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("Guessed incorrectly."))
            .andExpect(jsonPath("$.id").value(equalTo(gameId)))
            .andExpect(jsonPath("$.original_word").value(equalTo(originalWord)))
            .andExpect(jsonPath("$.scramble_word").isNotEmpty())
            .andExpect(jsonPath("$.guess_word").doesNotExist())
            .andExpect(jsonPath("$.total_words").value(greaterThan(0)))
            .andExpect(jsonPath("$.remaining_words").value(greaterThan(0)))
            .andExpect(jsonPath("$.guessed_words").isArray())
            .andExpect(jsonPath("$.guessed_words").isEmpty());

    }

    @Test
    void givenCreateNewGame_whenSubmitWrongWord_thenGuessedIncorrectly() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is some value (that is not correct answer)
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Guessed incorrectly."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` equals to input `guessWord`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is equals to `remainingWords` of previous game state (no change)
         * i) `guessedWords` is empty list (because this is first attempt)
         */
        
         MvcResult newGameResult = this.mvc.perform(get("/api/game/new"))
            .andExpect(status().isOk())
            .andReturn();

        String newGameJson = newGameResult.getResponse().getContentAsString();
        String gameId = OM.readTree(newGameJson).get("id").asText();
        String originalWord = OM.readTree(newGameJson).get("original_word").asText();

        GameGuessInput input = new GameGuessInput();
        input.setId(gameId);
        input.setWord("wrongword");

        String requestBody = OM.writeValueAsString(input);

        this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("Guessed incorrectly."))
            .andExpect(jsonPath("$.id").value(equalTo(gameId)))
            .andExpect(jsonPath("$.original_word").value(equalTo(originalWord)))
            .andExpect(jsonPath("$.scramble_word").isNotEmpty())
            .andExpect(jsonPath("$.guess_word").value(equalTo(input.getWord())))
            .andExpect(jsonPath("$.total_words").value(greaterThan(0)))
            .andExpect(jsonPath("$.remaining_words").value(greaterThan(0)))
            .andExpect(jsonPath("$.guessed_words").isArray())
            .andExpect(jsonPath("$.guessed_words").isEmpty());
    }

    @Test
    void givenCreateNewGame_whenSubmitFirstCorrectWord_thenGuessedCorrectly() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is of correct answer
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Guessed correctly."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` equals to input `guessWord`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is equals to `remainingWords - 1` of previous game state (decrement by 1)
         * i) `guessedWords` is not empty list
         * j) `guessWords` contains input `guessWord`
         */
        
        MvcResult newGameResult = this.mvc.perform(get("/api/game/new"))
            .andExpect(status().isOk())
            .andReturn();

        String newGameJson = newGameResult.getResponse().getContentAsString();
        String gameId = OM.readTree(newGameJson).get("id").asText();
        String originalWord = OM.readTree(newGameJson).get("original_word").asText();
        Collection<String> generatedList = this.jumbleEngine.generateSubWords(originalWord, null);

        GameGuessInput input = new GameGuessInput();
        input.setId(gameId);
        input.setWord(generatedList.iterator().next());

        String requestBody = OM.writeValueAsString(input);
        int totalWords = OM.readTree(newGameJson).get("total_words").asInt();
        int remainingWords = OM.readTree(newGameJson).get("remaining_words").asInt();

        this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("Guessed correctly."))
            .andExpect(jsonPath("$.id").value(equalTo(input.getId())))
            .andExpect(jsonPath("$.original_word").value(equalTo(originalWord)))
            .andExpect(jsonPath("$.scramble_word").isNotEmpty())
            .andExpect(jsonPath("$.guess_word").value(equalTo(input.getWord())))
            .andExpect(jsonPath("$.total_words").value(equalTo(totalWords)))
            .andExpect(jsonPath("$.remaining_words").value(equalTo(remainingWords - 1)))
            .andExpect(jsonPath("$.guessed_words").isArray())
            .andExpect(jsonPath("$.guessed_words").isNotEmpty())
            .andExpect(jsonPath("$.guessed_words[0]").value(equalTo(input.getWord())));
    }

    @Test
    void givenCreateNewGame_whenSubmitAllCorrectWord_thenAllGuessed() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         * b) has submit all correct answers, except the last answer
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is of the last correct answer
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "All words guessed."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` equals to input `guessWord`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is 0 (no more remaining, game ended)
         * i) `guessedWords` is not empty list
         * j) `guessWords` contains input `guessWord`
         */
        
        MvcResult newGameResult = this.mvc.perform(get("/api/game/new"))
            .andExpect(status().isOk())
            .andReturn();

        String newGameJson = newGameResult.getResponse().getContentAsString();
        String gameId = OM.readTree(newGameJson).get("id").asText();
        String originalWord = OM.readTree(newGameJson).get("original_word").asText();
        Collection<String> generatedList = this.jumbleEngine.generateSubWords(originalWord, null);

        for(int i = 0; i < generatedList.size(); i++) {
            GameGuessInput input = new GameGuessInput();
            input.setId(gameId);
            input.setWord(generatedList.toArray(new String[0])[i]);

            String requestBody = OM.writeValueAsString(input);

            if(i == generatedList.size() - 1) {
                this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(equalTo(input.getId())))
                    .andExpect(jsonPath("$.original_word").value(equalTo(originalWord)))
                    .andExpect(jsonPath("$.scramble_word").isNotEmpty())
                    .andExpect(jsonPath("$.guess_word").value(equalTo(input.getWord())))
                    .andExpect(jsonPath("$.total_words").value(greaterThan(0)))
                    .andExpect(jsonPath("$.remaining_words").value(0))
                    .andExpect(jsonPath("$.guessed_words").isArray())
                    .andExpect(jsonPath("$.guessed_words").isNotEmpty())
                    .andExpect(jsonPath("$.result").value("All words guessed."));
            } else {
                this.mvc.perform(post("/api/game/guess").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(equalTo(input.getId())))
                    .andExpect(jsonPath("$.original_word").value(equalTo(originalWord)))
                    .andExpect(jsonPath("$.scramble_word").isNotEmpty())
                    .andExpect(jsonPath("$.guess_word").value(equalTo(input.getWord())))
                    .andExpect(jsonPath("$.total_words").value(greaterThan(0)))
                    .andExpect(jsonPath("$.remaining_words").value(greaterThan(0)))
                    .andExpect(jsonPath("$.guessed_words").isArray())
                    .andExpect(jsonPath("$.guessed_words").isNotEmpty())
                    .andExpect(jsonPath("$.result").value("Guessed correctly."));
            }
        }
    }

}
