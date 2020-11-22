package service;

import com.atmmachine.dao.BankDao;
import com.atmmachine.exceptions.CardNotFoundException;
import com.atmmachine.model.Card;
import com.atmmachine.model.request.ChangePinRequest;
import com.atmmachine.service.AccountService;
import com.atmmachine.service.CardService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CardServiceTest {
    
    private static final String NEW_PIN_INVALID_LENGTH = "12345";
    private static final String NEW_PIN_INVALID_CHARS = "A42@";
    private static final String RECEIVED_OLD_PIN = "4359";
    private static final String EXPECTED_OLD_PIN = "4358";
    private static final String VALID_NEW_PIN = "3498";
    private static final int TEST_CARD_ID = 677323;
    private CardService cardService;
    
    @Mock
    private BankDao bankDao;
    
    @Mock
    private AccountService accountService;
    
    @Before
    public void setUp() {
        cardService = new CardService(bankDao, accountService);
    }
    
    @Test
    public void testGetCardThrowsExceptionWhenNoCardIsFound() throws CardNotFoundException {
        doThrow(new CardNotFoundException()).when(bankDao).getCard(eq(TEST_CARD_ID));

        assertThatExceptionOfType(CardNotFoundException.class)
                .isThrownBy(() -> cardService.getCard(TEST_CARD_ID));
    }
    
    @Test
    public void testChangePinThrowsIllegalArgumentExceptionWhenPinIsInvalid() {
        ChangePinRequest changePinRequest = new ChangePinRequest();
        changePinRequest.setNewPin(NEW_PIN_INVALID_LENGTH);
        
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> cardService.changePin(changePinRequest, TEST_CARD_ID));
        
        changePinRequest.setNewPin(NEW_PIN_INVALID_CHARS);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> cardService.changePin(changePinRequest, TEST_CARD_ID));
    }

    @Test
    public void testChangePinThrowsIllegalArgumentExceptionWhenOldPinDoesNotMatch() 
            throws CardNotFoundException {
        ChangePinRequest changePinRequest = new ChangePinRequest();

        changePinRequest.setOldPin(RECEIVED_OLD_PIN);
        changePinRequest.setNewPin(VALID_NEW_PIN);

        Card card = new Card();
        card.setId(TEST_CARD_ID);
        card.setPin(EXPECTED_OLD_PIN);

        when(bankDao.getCard(eq(TEST_CARD_ID))).thenReturn(card);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> cardService.changePin(changePinRequest, TEST_CARD_ID));
    }
    
    @Test
    public void testCheckIfCardIsAuthenticatedThrowsAccessDeniedException() {
        cardService.setCardId(null);

        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> cardService.checkIfCardIsAuthenticated());
    }
    
    @Test
    public void testDeauthenticate() {
        cardService.setCardId(TEST_CARD_ID);
        cardService.deauthenticate();
        
        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> cardService.checkIfCardIsAuthenticated());
    }
}
