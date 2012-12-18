package br.com.caelum.vraptor.plugin.spring.tx;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.resource.ResourceMethod;

public class TransactionInterceptorTest {

	private SpringTransactionInterceptor interceptor;

	@Mock
	private Validator validator;

	@Mock
	private PlatformTransactionManager txManager;

	@Mock
	private InterceptorStack stack;

	@Mock
	private ResourceMethod method;

	@Mock
	private TransactionStatus status;

	private Object instance;

	@Test
	public void shouldStartAndCommitTransaction() throws Exception {
		interceptor.intercept(stack, method, instance);

		InOrder inOrder = inOrder(stack, txManager);
		inOrder.verify(stack).next(method, instance);
		inOrder.verify(txManager).commit(any(TransactionStatus.class));
	}

	@Test
	public void shouldRollbackTransactionIfItsNotCompletedWhenExecutionFinishes() throws Exception {
		when(status.isCompleted()).thenReturn(false);

		interceptor.intercept(stack, method, instance);

		verify(txManager).rollback(status);
	}

	@Test
	public void shouldRollbackIfValidatorHasErrors() {
		when(status.isCompleted()).thenReturn(false);
		when(validator.hasErrors()).thenReturn(true);

		interceptor.intercept(stack, method, instance);

		verify(txManager).rollback(any(TransactionStatus.class));
	}

	@Test
	public void shouldInterceptsAllMethods() {
		assertTrue("Deveria estar interceptando todos os métodos.", interceptor.accepts(null));
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(txManager.getTransaction(anyTransactionDefinition())).thenReturn(status);

		interceptor = new SpringTransactionInterceptor(txManager, validator);
	}

	private TransactionDefinition anyTransactionDefinition() {
		return any(TransactionDefinition.class);
	}
}