package ru.mentorbank.backoffice.services.moneytransfer;

//import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mentorbank.backoffice.dao.exception.OperationDaoException;
import ru.mentorbank.backoffice.model.Operation;
import ru.mentorbank.backoffice.model.stoplist.PhysicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.StopListInfo;
import ru.mentorbank.backoffice.model.stoplist.StopListStatus;
import ru.mentorbank.backoffice.model.transfer.AccountInfo;
import ru.mentorbank.backoffice.model.transfer.PhysicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.TransferRequest;
import ru.mentorbank.backoffice.services.accounts.AccountService;
import ru.mentorbank.backoffice.services.accounts.AccountServiceBean;
import ru.mentorbank.backoffice.services.moneytransfer.exceptions.TransferException;
import ru.mentorbank.backoffice.services.stoplist.StopListService;
import ru.mentorbank.backoffice.services.stoplist.StopListServiceStub;
import ru.mentorbank.backoffice.test.AbstractSpringTest;
import ru.mentorbank.backoffice.dao.stub.OperationDaoStub;

public class MoneyTransferServiceTest extends AbstractSpringTest {

	@Autowired
	private MoneyTransferServiceBean moneyTransferService;
	private OperationDaoStub mockedOperationDao;
	
	@Before
	public void setUp() {
		mockedOperationDao = mock(OperationDaoStub.class);
		 
	}
	
	@Test
	public void transfer() throws TransferException, OperationDaoException {
	    
	    	StopListService mockedStopListService = mock(StopListServiceStub.class);
		AccountService mockedAccountService = mock(AccountServiceBean.class);
		
		
		 
		PhysicalAccountInfo srcPhysicalAccountInfo = new PhysicalAccountInfo();
		PhysicalAccountInfo dstPhysicalAccountInfo = new PhysicalAccountInfo();
		
		srcPhysicalAccountInfo.setAccountNumber("111111111111111");
		srcPhysicalAccountInfo.setDocumentSeries(StopListServiceStub.DOC_SERIES_FOR_OK_STATUS);//Number
		srcPhysicalAccountInfo.setDocumentNumber(StopListServiceStub.DOC_NUMBER_FOR_OK_STATUS);
		dstPhysicalAccountInfo.setAccountNumber("222222222222222");
		srcPhysicalAccountInfo.setDocumentSeries(StopListServiceStub.DOC_SERIES_FOR_OK_STATUS);
		dstPhysicalAccountInfo.setDocumentNumber(StopListServiceStub.DOC_NUMBER_FOR_OK_STATUS);

		TransferRequest request = new TransferRequest();
		
		request.setSrcAccount(srcPhysicalAccountInfo);
		request.setDstAccount(dstPhysicalAccountInfo);
		
		moneyTransferService.setStopListService(mockedStopListService);
		moneyTransferService.setAccountService(mockedAccountService);
		moneyTransferService.setOperationDao(mockedOperationDao);

		StopListInfo okStopListInfo = new StopListInfo();
		okStopListInfo.setStatus(StopListStatus.OK);
		when(mockedStopListService.getPhysicalStopListInfo(any(PhysicalStopListRequest.class))).thenReturn(okStopListInfo);
		
		when(mockedAccountService.verifyBalance(any(AccountInfo.class))).thenReturn(true);
	
		
		moneyTransferService.transfer(request);
		
		verify(mockedAccountService).verifyBalance(
			any(AccountInfo.class));
			//srcPhysicalAccountInfo);// Actual inocation has different arguments
		verify(mockedStopListService, times(2)).getPhysicalStopListInfo(any(PhysicalStopListRequest.class));
		verify(mockedOperationDao).saveOperation(any(Operation.class));

	}
}
