package weather.web.controller;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import weather.domain.Favorite;
import weather.service.FavoriteNotFoundException;
import weather.service.FavoriteService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class FavoriteControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private FavoriteController controller;

    @Mock
    private FavoriteService service;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void index_ShouldIncludeFavoritesInModel() throws Exception {
        // Arrange the mock behavior
        List<Favorite> favorites = Arrays.asList(
                new Favorite.FavoriteBuilder(1L).withAddress("Chicago").withPlaceId("chicago1").build(),
                new Favorite.FavoriteBuilder(2L).withAddress("Omaha").withPlaceId("omaha1").build()
        );
        when(service.findAll()).thenReturn(favorites);

        // Act (perform the MVC request) and Assert results
        mockMvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("favorite/index"))
                .andExpect(model().attribute("favorites", favorites));
        verify(service).findAll();
    }

    @Test
    public void add_ShouldRedirectToNewFavorite() throws Exception {
        // Arrange the mock behavior
        doAnswer(invocation -> {
            Favorite f = (Favorite) invocation.getArguments()[0];
            f.setId(1L);
            return null;
        }).when(service).save(any(Favorite.class));

        // Act (perform the MVC request) and Assert results
        mockMvc.perform(
                post("/favorites")
                        .param("formattedAddress", "chicago, il")
                        .param("placeId", "windycity")
        ).andExpect(redirectedUrl("/favorites/1"));
        verify(service).save(any(Favorite.class));
    }

    @Test
    public void detail_ShouldErrorOnNotFound() throws Exception {
        // Arrange the mock behavior
        when(service.findById(1L)).thenThrow(FavoriteNotFoundException.class);

        // Act (perform the MVC request) and Assert results
        mockMvc.perform(get("/favorites/1"))
                .andExpect(view().name("error"))
                .andExpect(model().attribute("ex", Matchers.instanceOf(FavoriteNotFoundException.class)));
        verify(service).findById(1L);
    }
}