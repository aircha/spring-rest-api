package me.aircha.restapi.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
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

    @Test
    @Parameters
    public void freeTest(int basePrice, int maxPrice, boolean free) {
        // given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // when
        event.update();

        // then
        assertThat(event.isFree()).isEqualTo(free);
    }

    private Object[] parametersForFreeTest() {
        return new Object[] {
                new Object[] {0, 0, true},
                new Object[] {100, 0, false},
                new Object[] {0, 100, false},
                new Object[] {100, 200, false}
        };
    }

    @Test
    @Parameters
    public void testOffline(String location, boolean offline) {
        // given
        Event event = Event.builder()
                .location(location)
                .build();

        // when
        event.update();

        // then
        assertThat(event.isOffline()).isEqualTo(offline);
    }

    private Object[] parametersForTestOffline() {
        return new Object[] {
                new Object[] {"판교 유스페이스몰2", true},
                new Object[] {null, false},
                new Object[] {"        ", false}
        };
    }
}