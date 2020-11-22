package com.atmmachine.controller;

import com.atmmachine.constants.BankConstants;
import com.atmmachine.model.BankAccount;
import com.atmmachine.model.Currency;
import com.atmmachine.service.AccountService;
import com.atmmachine.service.BankService;
import com.atmmachine.service.CardService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.AccessDeniedException;

@WebMvcTest(BankController.class)
public class BankControllerTest {
    
    private static final int TEST_CARD_ID = 4657321;

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BankService bankService;
    
    @MockBean
    private AccountService accountService;
    
    @MockBean
    private CardService cardService;

    @Test
    public void testGetBalanceHappyFlow() throws Exception {
        when(cardService.getCardId()).thenReturn(TEST_CARD_ID);
        
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(0.00);
        bankAccount.setCurrency(Currency.RON);
        
        when(accountService.getAccountByCardId(eq(TEST_CARD_ID))).thenReturn(bankAccount);
        
        this.mockMvc
                .perform(get("/atmOperation"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(bankAccount.getBalance() + " " 
                                            + bankAccount.getCurrency() + " left")
                );
    }


    @Test
    public void testGetBalanceReturnsUnauthorized() throws Exception {
        doThrow(new AccessDeniedException(BankConstants.CARD_NOT_INSERTED))
                .when(cardService).checkIfCardIsAuthenticated();

        this.mockMvc
                .perform(get("/atmOperation"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}