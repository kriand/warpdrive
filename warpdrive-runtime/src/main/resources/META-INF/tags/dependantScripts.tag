<%@ taglib prefix="wd" uri="http://kristianandersen.net/warpdrive/taglib" %><%@ tag body-content="scriptless" %><jsp:doBody scope="page" var="warpdriveBufferedScripts" />${wd:bufferScripts(pageContext.request, pageScope.warpdriveBufferedScripts)}