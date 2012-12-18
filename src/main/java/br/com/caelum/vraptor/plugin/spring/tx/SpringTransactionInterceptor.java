package br.com.caelum.vraptor.plugin.spring.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.resource.ResourceMethod;

@Intercepts
public class SpringTransactionInterceptor implements Interceptor {

	private final PlatformTransactionManager transactionManager;

	private final Validator validator;

	public SpringTransactionInterceptor(PlatformTransactionManager transactionManager, Validator validator) {
		this.transactionManager = transactionManager;
		this.validator = validator;
	}

	public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance)
			throws InterceptionException {

		TransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);

		stack.next(method, resourceInstance);

		try {
			if (!validator.hasErrors()) {
				transactionManager.commit(status);
			}
		} finally {
			if (!status.isCompleted()) {
				transactionManager.rollback(status);
			}
		}
	}

	public boolean accepts(ResourceMethod method) {
		return true;
	}

}
