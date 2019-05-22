<#-- @ftlvariable name="mitteilung" type="ch.dvbern.ebegu.entities.Mitteilung" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${mitteilung.empfaenger.fullName} <${empfaengerMail}>
Subject: <@base64Header>kiBon - Neue Nachricht der Gemeinde ${mitteilung.dossier.gemeinde.name} / Nouveau message de la commune ${mitteilung.dossier.gemeinde.name}</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - Neue Nachricht der Gemeinde ${mitteilung.dossier.gemeinde.name} / Nouveau message de la commune ${mitteilung.dossier.gemeinde.name}</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Die Gemeinde ${mitteilung.dossier.gemeinde.name} hat Ihnen eine
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/mitteilungen/${mitteilung.dossier.fall.id}/${mitteilung.dossier.id}/">Nachricht</a>
		geschrieben.
	</p>
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${mitteilung.dossier.gemeinde.name}
	</p>
	<p>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>

	<hr>

	<p>
		Chère famille,
	</p>
	<p>
		Votre commune vous a envoyé un <a href="<#if configuration
	.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/mitteilungen/${mitteilung.dossier.fall.id}/${mitteilung.dossier.id}/">message</a>.
	</p>
	<p>
		Nous vous présentons nos salutations les meilleures.<br/>
		Votre commune
	</p>
	<p>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>
