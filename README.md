# Task Manager

Aplicación web para gestionar tareas, creada como proyecto de práctica para aprender desarrollo full stack con Docker.

## Tecnologías usadas

### Frontend
- React
- Vite
- JavaScript
- CSS

### Backend
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Validation
- PostgreSQL Driver

### Base de datos
- PostgreSQL 16

### DevOps / Entorno
- Docker
- Docker Compose
- Git

## Funcionalidades

- Crear tareas
- Listar tareas
- Editar tareas
- Marcar tareas como completadas
- Eliminar tareas
- Validación de datos
- Manejo global de errores
- Backend y frontend dockerizados

## Estructura del proyecto

```text
01-task-manager/
  backend/
  frontend/
  database/
  docker-compose.yml
  README.md
```

## Levantar el proyecto con Docker

Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

Ver contenedores activos:

```bash
docker ps
```

Servicios esperados:

```text
task-manager-web  -> http://localhost:5173
task-manager-api  -> http://localhost:8080
task-manager-db   -> PostgreSQL en puerto 5432
```

## Endpoints principales

### Listar tareas

```http
GET /api/tasks
```

### Buscar tarea por ID

```http
GET /api/tasks/{id}
```

### Crear tarea

```http
POST /api/tasks
```

Body:

```json
{
  "title": "Nueva tarea",
  "description": "Descripción de la tarea",
  "completed": false
}
```

### Actualizar tarea

```http
PUT /api/tasks/{id}
```

Body:

```json
{
  "title": "Tarea actualizada",
  "description": "Nueva descripción",
  "completed": true
}
```

### Eliminar tarea

```http
DELETE /api/tasks/{id}
```

## Comandos útiles

Detener contenedores:

```bash
docker compose down
```

Levantar nuevamente:

```bash
docker compose up -d
```

Ver logs del backend:

```bash
docker logs task-manager-api
```

Ver logs del frontend:

```bash
docker logs task-manager-web
```

Entrar a PostgreSQL:

```bash
docker exec -it task-manager-db psql -U admin -d task_manager
```

Consultar tareas en base de datos:

```sql
select * from tasks;
```

## Estado actual

Proyecto full stack funcional con frontend, backend y base de datos ejecutándose mediante Docker Compose.

````