// json-parse.groovy

def parser = new groovy.json.JsonSlurper()
def json = parser.parseText(context.request.body)

respond().withData(json.hello)
