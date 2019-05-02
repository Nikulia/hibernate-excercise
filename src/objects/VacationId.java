package objects;

import java.io.Serializable;
import java.util.Objects;

public class VacationId implements Serializable {
    Integer year;
    Employee employee;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationId that = (VacationId) o;
        return Objects.equals(year, that.year) &&
                Objects.equals(employee, that.employee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, employee);
    }
}
