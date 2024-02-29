package ke.co.skyworld.rest;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import ke.co.skyworld.handlers.company.*;
import ke.co.skyworld.handlers.department.*;
import ke.co.skyworld.handlers.earnings.*;
import ke.co.skyworld.handlers.period.*;
import ke.co.skyworld.rest.base.*;

public class Routes {
    public static RoutingHandler Company() {
        return Handlers.routing()
                .post( "",new Dispatcher(new BlockingHandler(new CreateCompany())))
                .put( "/{companyId}",new Dispatcher(new BlockingHandler(new UpdateCompany())))
                .get( "/{companyId}", new Dispatcher(new GetCompany()))
                .get("", new Dispatcher(new GetCompanies()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Department() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateDepartment())))
                .put( "/{departmentId}",new Dispatcher(new BlockingHandler(new UpdateDepartment())))
                .get( "/{departmentId}", new Dispatcher(new GetDepartment()))
                .get("",new Dispatcher(new GetDepartments()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
//
    public static RoutingHandler Period() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreatePeriod())))
                .put( "/{periodId}",new Dispatcher(new BlockingHandler(new UpdatePeriod())))
                .get( "/active", new Dispatcher(new FetchActivePeriod()))
                .get("/{periodId}",new Dispatcher(new FetchPeriod()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
//
    public static RoutingHandler Earning() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateEarnings())))
                .put( "/{earningTypeId}",new Dispatcher(new BlockingHandler(new UpdateEarning())))
                .get( "/", new Dispatcher(new GetEarning()))
//                .get("",new Dispatcher(new GetExams()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
//
//    public static RoutingHandler ExamSchedule() {
//        return Handlers.routing()
//                .post( "", new Dispatcher(new BlockingHandler(new CreateExamSchedules())))
//                .put( "/{examScheduleId}",new Dispatcher(new BlockingHandler(new UpdateExamSchedule())))
//                .get( "/{examScheduleId}", new Dispatcher(new GetExamSchedule()))
//                .get("",new Dispatcher(new GetExamSchedules()))
//                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
//                .setFallbackHandler(new Dispatcher(new FallBack()));
//    }
//
//    public static RoutingHandler Subject() {
//        return Handlers.routing()
//                .post( "", new Dispatcher(new BlockingHandler(new CreateSubject())))
//                .put( "/{subjectId}",new Dispatcher(new BlockingHandler(new UpdateSubject())))
//                .get( "/{subjectId}", new Dispatcher(new GetSubject()))
//                .get("",new Dispatcher(new GetSubjects()))
//                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
//                .setFallbackHandler(new Dispatcher(new FallBack()));
//    }
//
//    public static RoutingHandler Answers() {
//        return Handlers.routing()
//                .post( "", new Dispatcher(new BlockingHandler(new CreateAnswer())));
//    }
//
//    public static RoutingHandler Question() {
//        return Handlers.routing()
//                .post( "", new Dispatcher(new BlockingHandler(new CreateQuestion())))
//                .put( "/{questionId}",new Dispatcher(new BlockingHandler(new UpdateQuestion())))
//                .get( "/{examSubjectId}/{questionId}", new Dispatcher(new GetQuestion()))
//                .get("/{examSubjectId}",new Dispatcher(new GetQuestions()))
//                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
//                .setFallbackHandler(new Dispatcher(new FallBack()));
//    }
//
//    public static RoutingHandler Choice() {
//        return Handlers.routing()
//                .post( "", new Dispatcher(new BlockingHandler(new CreateChoice())))
//                .put( "/{choiceId}",new Dispatcher(new BlockingHandler(new UpdateChoice())))
//                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
//                .setFallbackHandler(new Dispatcher(new FallBack()));
//    }
//
//    public static RoutingHandler Report() {
//        return Handlers.routing()
//                .get( "/exams-by-teacher/{teacherId}", new Dispatcher(new BlockingHandler(new GenerateExamsByTeacher())))
//                .get( "/generate-answers/{examSubjectId}/{pupilId}",new Dispatcher(new BlockingHandler(new GeneratePupilsAnswers())))
//                .get( "/top-five-results/{examSubjectId}", new Dispatcher(new GenerateTopFivePupils()))
//                .get("/pupils-score/{examId}",new Dispatcher(new GeneratePupilScoreReport()))
//                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
//                .setFallbackHandler(new Dispatcher(new FallBack()));
//    }
}
