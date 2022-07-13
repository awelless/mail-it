<html lang="en">
<head>
    <title>${title}</title>
</head>
<body>
<h1>${title}</h1>
<p>${file.name} by ${file.developer}</p>

<ul>
    <#list systems as system>
    <li>${system_index + 1}. ${system.name} from ${system.developer}</li>
    </#if> <#-- invalid close tag -->
</ul>
</body>
</html>
