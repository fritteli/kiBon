<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="startDatum" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode" -->
<#-- @ftlvariable name="gruss" type="ch.dvbern.ebegu.entities.Gesuchsperiode" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Neue Periode freigeschaltet</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Neue Periode freigeschaltet</title>
</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Gerne möchten wir Sie mit dieser Mail informieren, dass die Periode ${gesuchsperiode.gesuchsperiodeString} ab sofort
		für die Erfassung Ihrer Daten in kiBon offen steht. <br>
	</p>
	<p>
		Bitte beachten Sie, dass der Betreuungsgutschein auf den Folgemonat nach Einreichung des vollständigen Antrags
		und ab Beginn des Betreuungsverhältnisses in der neuen Periode ausgestellt wird. D.h. ist direkt ab August ein Gutschein
		gewünscht, muss der vollständige Antrag spätestens im Juli eingereicht sein. Bitte füllen Sie wenn möglich die
		Steuererklärung aus, bevor Sie ihre finanziellen Verhältnisse in kiBon erfassen. Sie können alle notwendigen Angaben
		danach der Steuererklärung entnehmen und diese im Bereich «Dokumente» als Beleg für Ihre Angaben verwenden.
	</p>
	<p>
		<a href="https://www.kibon.ch/">Hier können Sie den neuen Antrag erfassen.</a>
	</p>
	<p>
		Falls Sie für die Periode ${gesuchsperiode.gesuchsperiodeString} kein Antrag stellen möchten, sind für Sie keine weiteren Schritte notwendig.
	<p>
		Freundliche Grüsse <br/>
		${gruss}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
