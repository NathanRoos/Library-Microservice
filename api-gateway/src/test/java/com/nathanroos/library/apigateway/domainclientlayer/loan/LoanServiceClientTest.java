package com.nathanroos.library.apigateway.domainclientlayer.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceClientTest {

    @InjectMocks
    private LoanServiceClient loanServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private final String host = "localhost";
    private final String port = "8080";

    @BeforeEach
    void setUp() {
        loanServiceClient = new LoanServiceClient(restTemplate, objectMapper, host, port);
    }

    @Test
    void testGetLoanByLoanId_Success() {
        String accountId = "acc123", loanId = "loan123";
        LoanResponseModel mockLoan = LoanResponseModel.builder()
                .loanId(loanId)
                .accountId(accountId)
                .build();
        when(restTemplate.getForObject(anyString(), eq(LoanResponseModel.class))).thenReturn(mockLoan);

        LoanResponseModel result = loanServiceClient.getLoanByLoanId(accountId, loanId);

        assertNotNull(result);
        assertEquals(loanId, result.getLoanId());
        assertEquals(accountId, result.getAccountId());
    }

    @Test
    void testGetLoanByLoanId_NotFound() throws Exception {
        String accountId = "acc123", loanId = "loan123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LoanResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/loans/" + loanId));

        assertThrows(NotFoundException.class, () -> loanServiceClient.getLoanByLoanId(accountId, loanId));
    }

    @Test
    void testGetAllLoans_Success() {
        String accountId = "acc123";
        List<LoanResponseModel> loans = List.of(LoanResponseModel.builder().loanId("loan123").accountId(accountId).build());
        ResponseEntity<List<LoanResponseModel>> response = new ResponseEntity<>(loans, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LoanResponseModel> result = loanServiceClient.getLoans(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllLoans_Empty() {
        String accountId = "acc123";
        var response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        var result = loanServiceClient.getLoans(accountId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddLoan_Success() {
        String accountId = "acc123";
        LoanRequestModel request = LoanRequestModel.builder()
                .accountId(accountId)
                .loanStatus(LoanStatusEnum.ACTIVE)
                .loanDate(new Date())
                .dueDate(new Date())
                .build();
        LoanResponseModel response = LoanResponseModel.builder().loanId("loan123").accountId(accountId).build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LoanResponseModel.class))).thenReturn(response);

        LoanResponseModel result = loanServiceClient.addLoan(accountId, request);

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
    }

    @Test
    void testAddLoan_InvalidInput() throws Exception {
        String accountId = "acc123";
        LoanRequestModel request = LoanRequestModel.builder().accountId(accountId).build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LoanResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid", "/loans"));

        assertThrows(InvalidInputException.class, () -> loanServiceClient.addLoan(accountId, request));
    }

    @Test
    void testUpdateLoan_Success() {
        String accountId = "acc123", loanId = "loan123";
        LoanRequestModel request = LoanRequestModel.builder().accountId(accountId).build();
        LoanResponseModel updated = LoanResponseModel.builder().loanId(loanId).accountId(accountId).build();
        when(restTemplate.getForObject(anyString(), eq(LoanResponseModel.class))).thenReturn(updated);

        LoanResponseModel result = loanServiceClient.updateLoan(accountId, request, loanId);

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
    }

    @Test
    void testDeleteLoan_Success() {
        String accountId = "acc123", loanId = "loan123";
        doNothing().when(restTemplate).delete(anyString());

        assertDoesNotThrow(() -> loanServiceClient.deleteLoan(accountId, loanId));
    }

    @Test
    void testDeleteLoan_NotFound() throws Exception {
        String accountId = "acc123", loanId = "loan123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/loans/" + loanId));

        assertThrows(NotFoundException.class, () -> loanServiceClient.deleteLoan(accountId, loanId));
    }

    @Test
    void testGetLoans_HttpError() throws Exception {
        String accountId = "acc123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> loanServiceClient.getLoans(accountId));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }
}
