// package be.ap.backend.controller;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;

// import be.ap.backend.dto.SharedListResponseDTO;
// import be.ap.backend.entity.BookList;
// import be.ap.backend.entity.SavedList;
// import be.ap.backend.service.SavedListService;
// import jakarta.servlet.http.HttpSession;

// @SpringBootTest
// public class SavedListControllerTest {

// @MockitoBean
// private SavedListService savedListService;

// @Autowired
// private SavedListController controller;

// private HttpSession mockSession(Long userId) {
// HttpSession session = mock(HttpSession.class);
// when(session.getAttribute("userId")).thenReturn(userId);
// return session;
// }

// @Test
// void givenSession_whenGetSavedLists_thenReturnSavedLists() {
// BookList list = new BookList();
// list.setId(1L);
// list.setName("Shared List");
// SharedListResponseDTO response = new SharedListResponseDTO(list, List.of());
// when(savedListService.getSavedLists(1L)).thenReturn(List.of(response));

// ResponseEntity<List<SharedListResponseDTO>> result =
// controller.getSavedLists(mockSession(1L));

// assertNotNull(result.getBody());
// assertEquals(1, result.getBody().size());
// verify(savedListService, times(1)).getSavedLists(1L);
// }

// @Test
// void givenSession_whenGetSavedLists_thenReturnEmpty_whenNone() {
// when(savedListService.getSavedLists(1L)).thenReturn(List.of());

// ResponseEntity<List<SharedListResponseDTO>> result =
// controller.getSavedLists(mockSession(1L));

// assertNotNull(result.getBody());
// assertTrue(result.getBody().isEmpty());
// }

// @Test
// void givenSessionAndToken_whenSaveList_thenReturnSavedList() {
// BookList list = new BookList();
// list.setId(1L);
// SavedList savedList = new SavedList();
// savedList.setUserId(1L);
// savedList.setBookListId(1L);

// when(savedListService.getListByToken("abc123")).thenReturn(list);
// when(savedListService.saveList(1L, 1L)).thenReturn(savedList);

// ResponseEntity<SavedList> result = controller.saveList(mockSession(1L),
// "abc123");

// assertNotNull(result.getBody());
// assertEquals(1L, result.getBody().getUserId());
// verify(savedListService, times(1)).getListByToken("abc123");
// verify(savedListService, times(1)).saveList(1L, 1L);
// }

// @Test
// void givenSessionAndBookListId_whenUnsaveList_thenReturn204() {
// doNothing().when(savedListService).unsaveList(1L, 1L);

// ResponseEntity<Void> result = controller.unsaveList(mockSession(1L), 1L);

// assertEquals(204, result.getStatusCode().value());
// verify(savedListService, times(1)).unsaveList(1L, 1L);
// }

// @Test
// void givenSessionAndBookListId_whenIsSaved_thenReturnTrue() {
// when(savedListService.isSaved(1L, 1L)).thenReturn(true);

// ResponseEntity<Boolean> result = controller.isSaved(mockSession(1L), 1L);

// assertNotNull(result.getBody());
// assertTrue(result.getBody());
// verify(savedListService, times(1)).isSaved(1L, 1L);
// }

// @Test
// void givenSessionAndBookListId_whenIsSaved_thenReturnFalse_whenNotSaved() {
// when(savedListService.isSaved(1L, 99L)).thenReturn(false);

// ResponseEntity<Boolean> result = controller.isSaved(mockSession(1L), 99L);

// assertNotNull(result.getBody());
// assertFalse(result.getBody());
// verify(savedListService, times(1)).isSaved(1L, 99L);
// }
// }