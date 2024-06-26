package com.linked.classbridge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.reservation.ReservationDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationDto.Request request;
    private Lesson lesson;
    private User user;
    private Reservation reservation;


    @BeforeEach
    void setUp() {
        request = new ReservationDto.Request();
        request.setLessonId(1L);
        request.setUserId(1L);

        lesson = new Lesson();
        user = new User();
        reservation = Reservation.createReservation(request, lesson, user);

    }

    @Test
    @DisplayName("예약 성공")
    void createReservation_Success() {
        when(lessonRepository.findById(request.getLessonId())).thenReturn(Optional.of(lesson));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation createdReservation = reservationService.createReservation(request);

        assertNotNull(createdReservation);
        verify(lessonRepository).findById(request.getLessonId());
        verify(userRepository).findById(request.getUserId());
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("예약 실패_수업 정보 없음")
    void createReservation_LessonNotFound() {
        when(lessonRepository.findById(request.getLessonId())).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            reservationService.createReservation(request);
        });

        assertEquals(ErrorCode.LESSON_NOT_FOUND, exception.getErrorCode());
        verify(lessonRepository).findById(request.getLessonId());
        verify(userRepository, never()).findById(1L);
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    @DisplayName("예약 실패_회원 정보 없음")
    void createReservation_UserNotFound() {
        when(lessonRepository.findById(request.getLessonId())).thenReturn(Optional.of(lesson));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            reservationService.createReservation(request);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(lessonRepository).findById(request.getLessonId());
        verify(userRepository).findById(request.getUserId());
        verify(reservationRepository, never()).save(reservation);
    }
}
