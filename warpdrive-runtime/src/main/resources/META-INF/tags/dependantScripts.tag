<%@ taglib prefix="wd" uri="http://kristianandersen.net/warpdrive/taglib" %><%@ tag body-content="scriptless" %><jsp:doBody scope="request" var="net.kristianandersen.warpdrive.scripts" />${wd:bufferScripts(pageContext.request)}