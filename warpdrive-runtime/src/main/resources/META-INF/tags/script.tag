<%@ taglib prefix="wd" uri="http://kriand.org/warpdrive/taglib" %><%@ attribute name="src" required="true" %><%@ attribute name="type" required="false" %><%@ tag dynamic-attributes="dynattrs" body-content="empty"%>${wd:getScriptTag(src, type, dynattrs, pageContext.request)}