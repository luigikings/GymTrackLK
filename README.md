# GymTrackLK

Aplicación Android desarrollada en **Kotlin** con **Jetpack Compose** que ayuda a registrar entrenamientos de fuerza, gestionar rutinas y seguir hábitos diarios incluso sin conexión. El proyecto sigue una arquitectura **MVVM + Repository**, persiste datos localmente con **Room** y aprovecha **Coroutines/Flow** para la gestión reactiva de estados.

## Características principales
- Navegación inferior con tres secciones: **Ejercicios**, **Rutinas** y **Perfil**.
- Gestión completa de ejercicios con búsqueda, filtros por categoría, notas, imágenes desde galería y cálculo automático de récords personales.
- Rutinas personalizables con orden configurable y flujo de entreno activo que guarda el estado para reanudar más tarde.
- Registro diario de entrenamientos y consumo de creatina, calendario de actividad y cálculo de rachas por días o semanas.
- Exportación/Importación de respaldo en JSON para operar offline-first.
- Temas claro/oscuro/sistema, preferencia de unidades (kg/lb) y recordatorio configurable de creatina.
- Implementación de accesibilidad básica mediante `contentDescription` e interfaces con tamaños táctiles cómodos.

## Arquitectura
- **Presentación:** Jetpack Compose + ViewModels por feature.
- **Datos:** Room (DAO + TypeConverters) y DataStore para preferencias.
- **Dominio:** Utilidades para PR, rachas y calendarios.
- **Imágenes:** Coil para cargar y actualizar fotos de ejercicios mediante Storage Access Framework.

## Estructura de navegación
- **Ejercicios:** lista, creación, detalle con calendario y acceso a historial de series.
- **Rutinas:** listado, editor y flujo de entreno con pantalla de felicitación cuando se completa la sesión.
- **Perfil:** calendario mensual, racha actual, hábitos de creatina, estadísticas y preferencias.

## Cómo compilar y ejecutar
1. Instala las dependencias con Android Studio Iguana o superior.
2. Sincroniza el proyecto (`Sync Project with Gradle Files`).
3. Compila o ejecuta en un dispositivo/emulador con Android 7.1 (API 25) o superior.
4. Las semillas de datos (ejercicios base y una rutina ejemplo) se cargan automáticamente en builds **debug**.

## Pruebas
- **Unitarias:** se encuentran en `app/src/test/...` y cubren comparador de récords, cálculo de rachas y marcado de calendario.
- **Instrumentadas:** `WorkoutFlowInstrumentedTest` valida el cierre de entrenos, creación de sesiones y actualización de récords en una base en memoria.

Ejecuta las pruebas desde Android Studio o con Gradle:
```bash
./gradlew testDebug
./gradlew connectedDebugAndroidTest
```

## Licencia
Proyecto creado para fines demostrativos. Si deseas reutilizarlo, menciona la fuente.
