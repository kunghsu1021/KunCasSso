package com.sso.server.authencation;

import com.kunghsu.verify.ICredentialVerifyService;
import com.sso.server.credential.BaseCredential;
import com.sso.server.filter.SessionFilter;
import com.sso.server.utils.SsoUtil;
import com.sso.server.vo.SsoLoginInfoVo;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static com.sso.server.constant.SsoContant.SSO_SESSION_KEY;

/**
 * 提供给非web应用进行Restful方式的调用
 * Created by xuyaokun On 2018/11/5 14:07
 *
 * @desc: 这个是供非页面登陆使用的，通过web-flow里的参数判断决定走的是这个Handler
 */
public class SimpleUsernamePasswordAuthenticationHandler extends BaseAbstractAuthencationHandler {
	private static final Logger log = LoggerFactory.getLogger(SimpleUsernamePasswordAuthenticationHandler.class);
	
    @Value("${login.single2_cipher_password}")
    private String single2CipherPassword;

	@Value("${login.single1_cipher_password}")
	private String single1CipherPassword;

	@Resource
	private ICredentialVerifyService credentialVerifyService;

	@Override
	protected HandlerResult doAuthentication(Credential credential) throws GeneralSecurityException, PreventedException {
		BaseCredential baseCredential = (BaseCredential) credential;

		//通过threadloacl记录的session,拿到当前线程的sessionID
		String ssoSessionId = SessionFilter.getRequest().getSession().getId();

		//从Credential中，我们可以拿到很多和登录用户相关的信息，有了这些信息，就可以为用户做登录

		//可以根据调用登录传过来的值，做特殊处理，例如我要做app登录还是桌面软件客户端登录
        String single = baseCredential.getSingle();//例如通过single这个自定义参数来做判断

		SsoLoginInfoVo ssoLoginInfoVo = credentialVerifyService.verify(baseCredential);
		//注意，登录成功，要创建session
		SsoUtil.createSession(ssoSessionId, ssoLoginInfoVo, "1");
		SessionFilter.getRequest().getSession().setAttribute(SSO_SESSION_KEY, ssoLoginInfoVo);


		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("APP_CLIENT", "true");
		Principal principal = this.principalFactory.createPrincipal(ssoSessionId, attributes);
		return createHandlerResult(credential, principal, null);
	}

}
