package help;

import loader.Loader;
import objects.Employee;

import java.time.LocalDate;
import java.util.Date;

public class IntersectionVacations {
    Employee firstEmployee;
    Employee secondEmployee;
    Date beginIntersection;
    Date endIntersection;

    public IntersectionVacations() {
    }

    public IntersectionVacations(Employee firstEmployee, Employee secondEmployee, Date beginIntersection, Date endIntersection) {
        this.firstEmployee = firstEmployee;
        this.secondEmployee = secondEmployee;
        this.beginIntersection = beginIntersection;
        this.endIntersection = endIntersection;
    }

    public Employee getFirstEmployee() {
        return firstEmployee;
    }

    public void setFirstEmployee(Employee firstEmployee) {
        this.firstEmployee = firstEmployee;
    }

    public Employee getSecondEmployee() {
        return secondEmployee;
    }

    public void setSecondEmployee(Employee secondEmployee) {
        this.secondEmployee = secondEmployee;
    }

    public Date getBeginIntersection() {
        return beginIntersection;
    }

    public void setBeginIntersection(Date beginIntersection) {
        this.beginIntersection = beginIntersection;
    }

    public Date getEndIntersection() {
        return endIntersection;
    }

    public void setEndIntersection(Date endIntersection) {
        this.endIntersection = endIntersection;
    }

    public int getYear() {
        return Loader.dateToLocalDate(beginIntersection).getYear();
    }
}
