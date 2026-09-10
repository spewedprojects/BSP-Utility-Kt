package com.gratus.bsputility.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    // --- EMPLOYEES ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>): List<Long>

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    // --- DAILY ATTENDANCE ---
    @Query("SELECT * FROM daily_attendance WHERE date = :date ORDER BY employeeName ASC")
    fun getAttendanceForDate(date: String): Flow<List<DailyAttendance>>

    @Query("SELECT * FROM daily_attendance WHERE date = :date AND employeeId = :employeeId LIMIT 1")
    suspend fun getAttendanceRecord(date: String, employeeId: Long): DailyAttendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: DailyAttendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceBatch(records: List<DailyAttendance>)

    @Query("DELETE FROM daily_attendance WHERE date = :date AND employeeId = :employeeId")
    suspend fun deleteAttendance(date: String, employeeId: Long)

    // --- DEPARTMENT VERIFICATIONS ---
    @Query("SELECT * FROM department_verifications WHERE date = :date ORDER BY departmentName ASC")
    fun getVerificationsForDate(date: String): Flow<List<DepartmentVerification>>

    @Query("SELECT * FROM department_verifications WHERE date = :date AND departmentName = :dept LIMIT 1")
    suspend fun getVerification(date: String, dept: String): DepartmentVerification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVerification(verification: DepartmentVerification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationsBatch(verifications: List<DepartmentVerification>)

    // --- CONTRACTORS ---
    @Query("SELECT * FROM contractors ORDER BY name ASC")
    fun getAllContractors(): Flow<List<Contractor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContractor(contractor: Contractor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContractors(contractors: List<Contractor>)

    @Update
    suspend fun updateContractor(contractor: Contractor)

    @Delete
    suspend fun deleteContractor(contractor: Contractor)

    // --- CONFIG ITEMS (DEPARTMENTS, DESIGNATIONS, UNITS, ROLES, CUSTOM FIELDS) ---
    @Query("SELECT * FROM config_items WHERE category = :category ORDER BY name ASC")
    fun getConfigItemsByCategory(category: String): Flow<List<ConfigItem>>

    @Query("SELECT * FROM config_items ORDER BY category ASC, name ASC")
    fun getAllConfigItems(): Flow<List<ConfigItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigItem(item: ConfigItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigItems(items: List<ConfigItem>)

    @Delete
    suspend fun deleteConfigItem(item: ConfigItem)
}
