package me.aircha.restapi.events;

import me.aircha.restapi.accounts.Account;
import me.aircha.restapi.accounts.AccountRepository;
import me.aircha.restapi.accounts.AccountRole;
import me.aircha.restapi.accounts.AccountService;
import me.aircha.restapi.common.BaseControllerTest;
import me.aircha.restapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @TestDescription("정상적으로 입력하는 경우의 테스트")
    public void createEvent() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("판교 유스페이스몰2")
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment of new event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment of new event"),
                                fieldWithPath("free").description("it tells is this event is free or not"),
                                fieldWithPath("offline").description("it tells is this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query events"),
                                fieldWithPath("_links.update-event.href").description("link to update existing event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
        ;
    }

    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    private String getAccessToken() throws Exception {
        String username = "jhcha@datastreams.co.kr";
        String password = "jhcha";
        Account jhcha = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(jhcha);

        String clientId = "myApp";
        String clientSecret = "pass";

        ResultActions perForm = this.mockMvc.perform(MockMvcRequestBuilders.post("/oauth/token")
                .with(httpBasic(clientId, clientSecret))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password"));

        var responseBody = perForm.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우의 테스트")
    public void createEventBadRequest() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("판교 유스페이스몰2")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON_UTF8)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 비어 있는 경우의 테스트")
    public void createEventBadRequestEmptyInput() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우의 테스트")
    public void createEventBadRequestBadRequestWrongInput() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("판교 유스페이스몰2")
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists());
    }

    @Test
    @TestDescription("30개 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvent() throws Exception {
        IntStream.range(0, 30).forEach(this::generateEvent);

        this.mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists());
    }

    @Test
    @TestDescription("기존 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {
        Event event = this.generateEvent(100);

        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"));
    }

    @Test
    @TestDescription("존재하지 않는 이벤트를 조회 했을때 404 응답받기")
    public void getEvent404() throws Exception {
        this.mockMvc.perform(get("/api/events/12323"))
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("정상적으로 이벤트를 수정")
    public void updateEvent() throws Exception {
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우 이벤트 수정 실패")
    public void updateEvent400empty() throws Exception {
        Event event = this.generateEvent(200);
        EventDto eventDto = new EventDto();

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 잘못된 경우 이벤트 수정 실패")
    public void updateEvent400wrong() throws Exception {
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("존재하지 않는 경우 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        this.mockMvc.perform(put("/api/events/{id}", "1121124")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("event " + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("판교 유스페이스몰2")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);
    }
}
