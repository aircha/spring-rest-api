package me.aircha.restapi.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {
    @Test
    public void builder() {
        // Given
        Event event = Event.builder()
                .name("jhcha")
                .description("spirng rest api")
                .build();

        // Then
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        // Given
        String name = "jhcha";
        String description = "spring rest api";

        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }
}