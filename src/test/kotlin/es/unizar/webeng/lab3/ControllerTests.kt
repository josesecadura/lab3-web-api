package es.unizar.webeng.lab3

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.Optional

private val MANAGER_REQUEST_BODY = { name: String ->
    """
    { 
        "role": "Manager", 
        "name": "$name" 
    }
    """
}

private val MANAGER_RESPONSE_BODY = { name: String, id: Int ->
    """
    { 
       "name" : "$name",
       "role" : "Manager",
       "id" : $id
    }
    """
}

private val MANAGER_LIST_BODY =
    """
    [{
       "name" : "Mary",
       "role" : "Manager",
       "id" : 1
    }]
    """

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ControllerTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var employeeRepository: EmployeeRepository

    @Test
    fun `POST is not safe and not idempotent`() {
        // SETUP - COMPLETE ME!
        // Initial state: the collection is empty. No employee exists.
        // Runtime state: the first save stores Mary, Manager, as id 1. The second save stores Mary, Manager, as id 2.
        // Simulamos la base de datos
        every {
            employeeRepository.save(any<Employee>())
        } answers {
            // Se asigna el id 1
            Employee("Mary", "Manager", 1)
        } andThenAnswer {
            // Se asigna id 2
            Employee("Mary", "Manager", 2)
        }

        mvc
            .post("/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Mary")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Mary", 1))
                }
            }

        mvc
            .post("/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Mary")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Location", "http://localhost/employees/2") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Mary", 2))
                }
            }

        // VERIFY - COMPLETE ME!
        // Hint: What repository methods should be called for a POST operation?
        // What methods should NOT be called? Think about the difference between safe and unsafe operations.
        // Comprobamos q se guardan dos veces el impleado
        verify(exactly = 2) {
            employeeRepository.save(Employee("Mary", "Manager", null))
        }
        // Comprobamos q nno hay consultas ni borrados
        verify(exactly = 0) {
            employeeRepository.findById(any())
            employeeRepository.findAll()
            employeeRepository.deleteById(any())
        }
    }

    @Test
    fun `GET is safe and idempotent`() {
        // SETUP
        // Initial state: /employees/1 is Mary, Manager, id 1. /employees/2 has no representation.
        // Runtime state: both stay that way.
        // Comprobar si existe el id 1
        every {
            employeeRepository.findById(1)
        } answers {
            Optional.of(Employee("Mary", "Manager", 1))
        }
        // El id no existe por eso esperamos el empty
        every {
            employeeRepository.findById(2)
        } answers {
            Optional.empty()
        }

        mvc.get("/employees/1").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_RESPONSE_BODY("Mary", 1))
            }
        }

        mvc.get("/employees/1").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_RESPONSE_BODY("Mary", 1))
            }
        }

        mvc.get("/employees/2").andExpect {
            status { isNotFound() }
        }

        mvc.get("/employees/2").andExpect {
            status { isNotFound() }
        }

        // VERIFY - COMPLETE ME!
        // Hint: Since GET is safe, what repository methods should NOT be called?
        // Count how many times each method was called based on the test requests.

        // Comprobar findById 1 se llama 2 veces
        verify(exactly = 2) {
            employeeRepository.findById(1)
        }

        // Comprobar que findById 2 se llama tmbn 2 veces
        verify(exactly = 2) {
            employeeRepository.findById(2)
        }

        // Comprobar q no se llama a nada que modifique el estado
        verify(exactly = 0) {
            employeeRepository.save(any())
            employeeRepository.deleteById(any())
            employeeRepository.findAll()
        }
    }

    @Test
    fun `PUT is idempotent but not safe`() {
        // SETUP
        // Initial state: /employees/1 has no representation.
        // Runtime state: the first PUT stores Tom, Manager, at id 1. The second PUT leaves that same employee.
        // La primera búsqueda no encuentra al empleado, la segunda si
        every {
            employeeRepository.findById(1)
        } answers {
            Optional.empty()
        } andThenAnswer {
            Optional.of(Employee("Tom", "Manager", 1))
        }

        // Simulamos que save devuelve a Tom con id 1
        every {
            employeeRepository.save(any<Employee>())
        } answers {
            Employee("Tom", "Manager", 1)
        }

        mvc
            .put("/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Tom")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Content-Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Tom", 1))
                }
            }

        mvc
            .put("/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Tom")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                header { string("Content-Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Tom", 1))
                }
            }

        // VERIFY - COMPLETE ME!
        // Hint: What repository methods should be called for PUT operations?
        // Think about the controller logic and how many times each method should be invoked.
        // Comprobamos que se busca con findById 1 dos veces
        verify(exactly = 2) {
            employeeRepository.findById(1)
        }

        // Comprobamos que se guarda Tom 2 veces
        verify(exactly = 2) {
            employeeRepository.save(Employee("Tom", "Manager", 1))
        }

        // Comprobamos que no se modifica nada mas
        verify(exactly = 0) {
            employeeRepository.deleteById(any())
            employeeRepository.findAll()
        }
    }

    @Test
    fun `DELETE is idempotent but not safe`() {
        // SETUP
        // Initial state: /employees/1 is associated with an employee.
        // Runtime state: the first DELETE removes that association. The second DELETE finds it already gone.

        // Simulamos eliminamos empleado id 1
        justRun {
            employeeRepository.deleteById(1)
        }

        mvc.delete("/employees/1").andExpect {
            status { isNoContent() }
        }

        mvc.delete("/employees/1").andExpect {
            status { isNoContent() }
        }

        // VERIFY
        // Hint: What repository methods should be called for DELETE operations?
        // What methods should NOT be called? Think about the nature of DELETE operations.
        // Comprobamos que se intenta eliminar el id 1 dos veces
        verify(exactly = 2) {
            employeeRepository.deleteById(1)
        }

        // Comprobamos que no se modifica nada mas
        verify(exactly = 0) {
            employeeRepository.save(any())
            employeeRepository.findById(any())
            employeeRepository.findAll()
        }
    }

    @Test
    fun `GET collection is safe and idempotent`() {
        // SETUP - COMPLETE ME!
        // Initial state: the collection contains exactly Mary, Manager, id 1.
        // Runtime state: the second GET sees that same list.
        // Simulamos que la lista contiene a Mary con id 1
        every {
            employeeRepository.findAll()
        } answers {
            listOf(Employee("Mary", "Manager", 1))
        }

        mvc.get("/employees").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_LIST_BODY)
            }
        }

        mvc.get("/employees").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_LIST_BODY)
            }
        }

        // VERIFY - COMPLETE ME!
        // Hint: Count the list calls. A safe list must not save, delete, or fetch one id.
        // Comprobamos que findAll se llama dos veces
        verify(exactly = 2) {
            employeeRepository.findAll()
        }

        // Comprobamos que no se edita
        verify(exactly = 0) {
            employeeRepository.save(any())
            employeeRepository.deleteById(any())
            employeeRepository.findById(any())
        }
    }
}
