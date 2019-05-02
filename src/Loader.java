import objects.Department;
import objects.Employee;
import objects.VacationId;
import objects.Vacations;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * Created by Danya on 26.10.2015.
 */
public class Loader
{
    private static final int COUNT_OF_YEARS_FOR_CALCULATE_VACATIONS = 2;//year
    private static final long FIRST_VACATION_FOR_NEW_EMPLOYEE_IN_MOUTHS = 6;//mouths
    private static final long SMALL_VACATION = 3;//weeks
    private static SessionFactory sessionFactory;

    public static void main(String[] args)
    {
        setUp();

        // create a couple of events...
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        System.out.println(isTableEmpty(session, "Vacations"));
        if(isTableEmpty(session, "Vacations")) {
            generateVacations(session);
        }

       /*List<Department> departments = (List<Department>) session.createQuery("FROM Department").list();
           for(Department department : departments) {
               System.out.println(department.getName());
       }*/

//        Department dept = new Department("Отдел производства");
//        session.save(dept);
//        System.out.println(dept.getId());

//        Department dept = (Department) session.createQuery("FROM Department WHERE name=:name")
//            .setParameter("name", "Отдел производства").list().get(0);
//        session.delete(dept);

        session.getTransaction().commit();
        session.close();

        //==================================================================
        if ( sessionFactory != null ) {
            sessionFactory.close();
        }
    }

    private static void generateVacations(Session session) {
        Random random = new Random(System.currentTimeMillis());
        List<Employee> employees = (List <Employee>)session.createQuery("FROM Employee").list();
        for (Employee employee: employees) {
            LocalDate beginPeriodVacation = LocalDate.parse(employee.getHireDate().toString())
                    .plusMonths(FIRST_VACATION_FOR_NEW_EMPLOYEE_IN_MOUTHS);
            LocalDate endPeriodVacation = LocalDate.now().plusYears(COUNT_OF_YEARS_FOR_CALCULATE_VACATIONS).withDayOfYear(1);
            for (int year = beginPeriodVacation.getYear(); year < endPeriodVacation.getYear(); year++) {
                Vacations vacation = new Vacations();
                VacationId vacationId = new VacationId();
                vacationId.setEmployee(employee);
                vacationId.setYear(year);
                vacation.setId(vacationId);
                if (year == beginPeriodVacation.getYear()) {
                    System.out.println(beginPeriodVacation);
                    vacation.setBeginVacationDate(java.sql.Date.valueOf(beginPeriodVacation.withDayOfYear(beginPeriodVacation.getDayOfYear() + random.nextInt(beginPeriodVacation.lengthOfYear() - beginPeriodVacation.getDayOfYear()))));

                    if (vacation.getBeginVacationDate().getTime() % 2 != 0)
                        vacation.setEndVacationDate(java.sql.Date.valueOf(LocalDate.parse(vacation.getBeginVacationDate().toString()).plusWeeks(SMALL_VACATION)));
                    System.out.println(beginPeriodVacation);
                }
            //LocalDate firstDayVacation = begin.plusf
            }
        }
    }

    private static boolean isTableEmpty(Session session, String table) {
        long rowCount = (long) session.createQuery("SELECT count(*) FROM " + table).uniqueResult();
        return rowCount == 0;
    }

    //=====================================================================

    private static void setUp()
    {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(new File("src/config/hibernate.cfg.xml")) // configures settings from hibernate.config.xml
                .build();
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            e.printStackTrace();
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
