package objects;

import java.util.Date;

public class Vacation {
    private VacationId id;
    private Date beginVacationDate;
    private Date endVacationDate;

    public VacationId getId() {
        return id;
    }

    public void setId(VacationId id) {
        this.id = id;
    }

    public Date getBeginVacationDate() {
        return beginVacationDate;
    }

    public void setBeginVacationDate(Date beginVacationDate) {
        this.beginVacationDate = beginVacationDate;
    }

    public Date getEndVacationDate() {
        return endVacationDate;
    }

    public void setEndVacationDate(Date endVacationDate) {
        this.endVacationDate = endVacationDate;
    }
}
