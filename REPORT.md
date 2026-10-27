# Lab 3 Web API -- Project Report

## What I specified

Se han modificado con lo entregado en el guión los test en controllerTest para definir el comportamiento de GET (seguro e idempotente), POST (no seguro ni idempotente), PUT  Y DELETE (no seguros si idempotentes).

## What I changed

- ControllerTests.kt

## Technical decisions

Las decisiones técnicas han ido guiadas por el guón de la práctica, usando every para definir las respuestas, justRun para deleteByID y verify para comprobar el número de llamadas siguiendo lo indicado por cada test.

## How I verified

Para verificar si funcionaba correctamente únicamente use el comando `./gradlew check` que al principio como nos mencionan en la guía falla y luego ya después de las modificaciones pasa correctamente.

## AI disclosure

- **Tools / skills:** ChatGPT
- **Purpose:** Resolver dudas sobre los test y entender la configuración y verificación
- **Representative prompts:** ¿Para que sirve every? ¿Que funcionalidades tienen estos test? ¿Como funciona justRun?
- **Affected files/sections:** ControllerTests.kt
- **Validation steps:** Contraste información que no me quedaba muy clara en Google.
- **Citations:** No aplica
- **Human-reviewed:** No aplica

Or: **No AI assistance** was used for this lab.
