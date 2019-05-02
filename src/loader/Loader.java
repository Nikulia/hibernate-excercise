package loader;

import help.IntersectionVacations;
import objects.Department;
import objects.Employee;
import objects.VacationId;
import objects.Vacation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by Danya on 26.10.2015.
 */
public class Loader {
    private static final int COUNT_OF_YEARS_FOR_CALCULATE_VACATIONS = 2;//year
    private static final long FIRST_VACATION_FOR_NEW_EMPLOYEE_IN_MOUTHS = 6;//mouths
    private static final long SMALL_VACATION = 3;//weeks
    private static final long LONG_VACATION = 4;//weeks
    private static final int MAX_LOW_SALARY = 115_000;
    private static final LocalDate HIRE_DATE_OLD_EMPLOYEE = LocalDate.of(2010, 03, 01);
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {
        setUp();

        // create a couple of events...
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        if (isTableEmpty(session, "Vacation")) {
            generateVacations(session);
            session.getTransaction().commit();
            session.close();
            session = sessionFactory.openSession();
            session.beginTransaction();
        }
        outputIntersectionsVacations(getIntersectionsVacations(session));
        outputStringCollection(getNamesWrongAttachedDepartmentHeads(session), "Неправильно прикреплённые " +
                "руководители отделов:");
        outputStringCollection(getNamesDepartmentHeadsWithSmallSalary(session, MAX_LOW_SALARY), "Руководители отделов " +
                "с зарплатой менее " + MAX_LOW_SALARY + ":");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.uuuu");
        outputStringCollection(getNamesHeadsDepartmentThatWorkLong(session, HIRE_DATE_OLD_EMPLOYEE),
                "Руководители отделов которые были наняты раньше " + HIRE_DATE_OLD_EMPLOYEE.format(formatter) + ":");


        session.getTransaction().commit();
        session.close();

        //==================================================================
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static boolean isTableEmpty(Session session, String table) {
        long rowCount = (long) session.createQuery("SELECT count(*) FROM " + table).uniqueResult();
        return rowCount == 0;
    }

    private static void generateVacations(Session session) {
        Random random = new Random(System.currentTimeMillis());
        List<Employee> employees = (List<Employee>) session.createQuery("FROM Employee").list();
        for (Employee employee : employees) {
            if (employee.getHireDate() == null)
                continue;
            LocalDate beginPeriodVacation = dateToLocalDate(employee.getHireDate())
                    .plusMonths(FIRST_VACATION_FOR_NEW_EMPLOYEE_IN_MOUTHS);
            LocalDate endPeriodVacation = LocalDate.now().plusYears(COUNT_OF_YEARS_FOR_CALCULATE_VACATIONS).withDayOfYear(1);
            for (int year = beginPeriodVacation.getYear(); year < endPeriodVacation.getYear(); year++) {
                Vacation vacation = new Vacation();
                VacationId vacationId = new VacationId();
                vacationId.setEmployee(employee);
                vacationId.setYear(year);
                vacation.setId(vacationId);
                vacation.setBeginVacationDate(generateBeginVacationDate(random, beginPeriodVacation));
                if (vacation.getBeginVacationDate().getTime() % 2 != 0)
                    vacation.setEndVacationDate(generateEndVacationDate(vacation, SMALL_VACATION));
                else
                    vacation.setEndVacationDate(generateEndVacationDate(vacation, LONG_VACATION));
                session.save(vacation);
                if (dateToLocalDate(vacation.getEndVacationDate()).getYear() == year)
                    beginPeriodVacation = beginPeriodVacation.plusYears(1).withDayOfYear(1);
                else
                    beginPeriodVacation = dateToLocalDate(vacation.getEndVacationDate());
            }
        }
    }

    public static LocalDate dateToLocalDate(java.util.Date date) {
        return LocalDate.parse(date.toString());
    }
    public static java.sql.Date localDateToDate(LocalDate date) {
        return Date.valueOf(date);
    }

    private static java.sql.Date generateBeginVacationDate(Random random, LocalDate beginPeriodVacation) {
        return localDateToDate(beginPeriodVacation.withDayOfYear(beginPeriodVacation.getDayOfYear() + random.nextInt(beginPeriodVacation.lengthOfYear() - beginPeriodVacation.getDayOfYear())));
    }

    private static Date generateEndVacationDate(Vacation vacation, long lengthVacationInWeeks) {
        return localDateToDate(dateToLocalDate(vacation.getBeginVacationDate()).plusWeeks(lengthVacationInWeeks));
    }


    private static List<IntersectionVacations> getIntersectionsVacations(Session session) {
        List<IntersectionVacations> intersections = new ArrayList<>();
        List<Department> departments = (List<Department>) session.createQuery("FROM Department").list();
        for (Department department : departments) {
            List<Vacation> vacations = (List<Vacation>) session.createQuery("FROM Vacation vac WHERE vac.id.year >= " +
                    ":this AND vac.id.employee.department.id = :department").
                    setParameter("this", LocalDate.now().getYear()).setParameter("department", department.getId()).list();
            for (int i = 0; i < vacations.size(); i++) {
                for (int j = 0; j < vacations.size(); j++) {
                    if (j == i)
                        continue;
                    if (isIntersectionVacations(vacations.get(i), vacations.get(j))) {
                        try {
                            if (isIntersectionAlreadyExist(vacations.get(i), vacations.get(j), intersections))
                                continue;
                            IntersectionVacations intersection = new IntersectionVacations(vacations.get(i).getId().getEmployee(),
                                    vacations.get(j).getId().getEmployee(), null, null);
                            if (isBeginFirstManVacationWithinSecondManVacation(vacations.get(i), vacations.get(j)))
                                intersection.setBeginIntersection(vacations.get(i).getBeginVacationDate());
                            else if (isBeginFirstManVacationWithinSecondManVacation(vacations.get(j), vacations.get(i)))
                                intersection.setBeginIntersection(vacations.get(j).getBeginVacationDate());
                            else
                                throwIntersectionException();
                            if (isEndFirstManVacationWithinSecondManVacation(vacations.get(i), vacations.get(j)))
                                intersection.setEndIntersection(vacations.get(i).getEndVacationDate());
                            else if (isEndFirstManVacationWithinSecondManVacation(vacations.get(j), vacations.get(i)))
                                intersection.setEndIntersection(vacations.get(j).getEndVacationDate());
                            else
                                throwIntersectionException();
                            intersections.add(intersection);
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }
            }
        }
        return intersections;
    }

    private static boolean isIntersectionVacations(Vacation firstManVacation, Vacation secondManVacation) {
        long beginFirstManVacation = dateToLocalDate(firstManVacation.getBeginVacationDate()).toEpochDay();
        long endFirstManVacation = dateToLocalDate(firstManVacation.getEndVacationDate()).toEpochDay();
        long beginSecondManVacation = dateToLocalDate(secondManVacation.getBeginVacationDate()).toEpochDay();
        long endSecondManVacation = dateToLocalDate(secondManVacation.getEndVacationDate()).toEpochDay();
        return beginFirstManVacation >= beginSecondManVacation && beginFirstManVacation <= endSecondManVacation
                || endFirstManVacation >= beginSecondManVacation && endFirstManVacation <= endSecondManVacation;
    }

    private static boolean isIntersectionAlreadyExist(Vacation firstVacation, Vacation secondVacation, List<IntersectionVacations> intersections) {
        for (IntersectionVacations intersection : intersections) {
            if (firstVacation.getId().getEmployee().equals(intersection.getFirstEmployee()) &&
                    secondVacation.getId().getEmployee().equals(intersection.getSecondEmployee()) &&
                    dateToLocalDate(firstVacation.getBeginVacationDate()).getYear() == (intersection.getYear()))
                return true;
            else if (secondVacation.getId().getEmployee().equals(intersection.getFirstEmployee()) &&
                    firstVacation.getId().getEmployee().equals(intersection.getSecondEmployee()) &&
                    dateToLocalDate(firstVacation.getBeginVacationDate()).getYear() == intersection.getYear())
                return true;
        }
        return false;
    }

    private static boolean isBeginFirstManVacationWithinSecondManVacation(Vacation firstManVacation, Vacation secondManVacation) {
        return firstManVacation.getBeginVacationDate().getTime() >= secondManVacation.getBeginVacationDate().getTime() &&
                firstManVacation.getBeginVacationDate().getTime() <= secondManVacation.getEndVacationDate().getTime();
    }

    private static boolean isEndFirstManVacationWithinSecondManVacation(Vacation firstManVacation, Vacation secondManVacation) {
        return firstManVacation.getEndVacationDate().getTime() <= secondManVacation.getEndVacationDate().getTime() &&
                firstManVacation.getEndVacationDate().getTime() >= secondManVacation.getBeginVacationDate().getTime();
    }

    private static void throwIntersectionException() {
        throw new IllegalStateException("This intersection was detected incorrectly!");
    }

    protected static void outputIntersectionsVacations(Collection<IntersectionVacations> intersections) {
        System.out.println("===============================================");
        if (intersections != null) {
            for (IntersectionVacations intersection : intersections) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                System.out.println(intersection.getFirstEmployee().getDepartment().getName() + "\n" + "Пересечение отпусков у сотрудников " +
                        intersection.getFirstEmployee().getName() + " и " +
                        intersection.getSecondEmployee().getName() + " в период с " +
                        dateFormat.format(intersection.getBeginIntersection()) + " по " +
                        dateFormat.format(intersection.getEndIntersection()));//Посмотреть!!!!
            }
        }
    }

    private static void outputStringCollection(Collection<String> collection, String message) {
        System.out.println("===============================================");
        System.out.println(message);
        for (String element : collection)
            System.out.println(element);
    }

    private static List<String> getNamesWrongAttachedDepartmentHeads(Session session) {
        return (List<String>) session.createQuery("SELECT employee.name FROM Employee employee, Department department " +
                "WHERE department.headDepartment.id = employee.id " +
                "AND employee.department.id != department.id)").list();
    }

    private static List<String> getNamesDepartmentHeadsWithSmallSalary(Session session, int maxLowSalary) {
        return (List<String>) session.createQuery("SELECT employee.name FROM Employee employee, Department department " +
                "WHERE department.headDepartment.id = employee.id " +
                "AND employee.salary < :maxSalary").setParameter("maxSalary", maxLowSalary).list();
    }

    private static List<String> getNamesHeadsDepartmentThatWorkLong(Session session, LocalDate hireDateOldEmployee) {
        return (List<String>) session.createQuery("SELECT employee.name FROM Employee employee, Department department " +
                "WHERE department.headDepartment.id = employee.id " +
                "AND employee.hireDate < :date").setParameter("date", Date.valueOf(hireDateOldEmployee)).list();
    }


    //=====================================================================

    private static void setUp() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(new File("src/config/hibernate.cfg.xml")) // configures settings from hibernate.config.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            e.printStackTrace();
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
