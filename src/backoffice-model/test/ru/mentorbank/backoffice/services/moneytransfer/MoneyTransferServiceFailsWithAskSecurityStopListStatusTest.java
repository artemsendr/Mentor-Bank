package ru.mentorbank.backoffice.services.moneytransfer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ExpectedException;

import ru.mentorbank.backoffice.model.stoplist.PhysicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.StopListInfo;
import ru.mentorbank.backoffice.model.stoplist.StopListStatus;
import ru.mentorbank.backoffice.model.transfer.JuridicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.TransferRequest;
import ru.mentorbank.backoffice.services.accounts.AccountService;
import ru.mentorbank.backoffice.services.accounts.AccountServiceBean;
import ru.mentorbank.backoffice.services.moneytransfer.exceptions.TransferException;
import ru.mentorbank.backoffice.services.stoplist.StopListService;
import ru.mentorbank.backoffice.services.stoplist.StopListServiceStub;
import ru.mentorbank.backoffice.test.AbstractSpringTest;

public class MoneyTransferServiceFailsWithAskSecurityStopListStatusTest extends
		AbstractSpringTest {

	@Autowired
	private MoneyTransferServiceBean moneyTransferService;
	private AccountService mockedAccountService;
	private JuridicalAccountInfo srcAccountInfo;
	private TransferRequest transferRequest;
	private JuridicalAccountInfo dstAccountInfo;
	private StopListService mockedStopListService;

	@Before
	public void setUp() {

		srcAccountInfo = new JuridicalAccountInfo();
		srcAccountInfo.setAccountNumber("111111111111111");
		srcAccountInfo.setInn(StopListServiceStub.INN_FOR_ASKSECURITY_STATUS);

		dstAccountInfo = new JuridicalAccountInfo();
		dstAccountInfo.setAccountNumber("222222222222222");
		dstAccountInfo.setInn(StopListServiceStub.INN_FOR_OK_STATUS);

		transferRequest = new TransferRequest();
		transferRequest.setSrcAccount(srcAccountInfo);
		transferRequest.setDstAccount(dstAccountInfo);

		mockedAccountService = mock(AccountServiceBean.class);
		// Dynamic Stub
		when(mockedAccountService.verifyBalance(dstAccountInfo)).thenReturn(
				true);
		moneyTransferService.setAccountService(mockedAccountService);
		
		mockedStopListService = mock(StopListService.class);
		StopListInfo asksequrityStopListInfo = new StopListInfo();
		asksequrityStopListInfo.setStatus(StopListStatus.ASKSECURITY);
		when(mockedStopListService.getPhysicalStopListInfo(any(PhysicalStopListRequest.class))).thenReturn(asksequrityStopListInfo);
	}
	
	@Test
	@ExpectedException(TransferException.class)
	public void transfer_failsWithAskSecurityStopListStatus()
			throws TransferException {
	    

		moneyTransferService.transfer(transferRequest);//when I run it with other tests it has the error "Null pointer exception"
		//verify(any(TransferException.class)).TransferException("Невозможно сделать перевод. Необходимо ручное вмешательство.");
		//
	}
}
