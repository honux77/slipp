package net.slipp.service.qna

import java.util.{HashSet, Set}

import com.google.common.collect.{Lists, Sets}
import net.slipp.domain.qna.{Answer, Question, QuestionDto}
import net.slipp.domain.tag.Tag
import net.slipp.domain.tag.TagTest.{JAVA, NEWTAG}
import net.slipp.domain.user.SocialUser
import net.slipp.repository.qna.{AnswerRepository, QuestionRepository}
import net.slipp.service.rank.ScoreLikeService
import net.slipp.service.tag.TagService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.{InjectMocks, Mock}
import org.mockito.Mockito.when
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.security.access.AccessDeniedException

@RunWith(classOf[MockitoJUnitRunner])
class QnaServiceTest {
  @Mock private var questionRepository: QuestionRepository = _
  @Mock private var answerRepository: AnswerRepository = _
  @Mock private var tagService: TagService = _
  @Mock private var scoreLikeService: ScoreLikeService = _
  @InjectMocks private val dut = new QnaService

  @Test def updateQuestion_sameUser {
    val loginUser: SocialUser = new SocialUser(10)
    val dto: QuestionDto = new QuestionDto(1L, "title", "contents", "java javascript")
    val originalTags: Set[Tag] = Sets.newHashSet(JAVA)
    val existedQuestion: Question = new Question(1L, loginUser, dto.getTitle, dto.getContents, originalTags)
    when(questionRepository.findOne(dto.getQuestionId)).thenReturn(existedQuestion)
    val tags: Set[Tag] = Sets.newHashSet(JAVA, NEWTAG)
    when(tagService.processTags(dto.getPlainTags)).thenReturn(tags)

    dut.updateQuestion(loginUser, dto)
  }

  @Test
  def deleteAnswer_sameUser {
    val loginUser: SocialUser = new SocialUser(10)
    val answer: Answer = new Answer(2L)
    answer.writedBy(loginUser)
    val answer2: Answer = new Answer(3L)
    val question: Question = new Question(1L, loginUser, null, null, new HashSet[Tag])
    question.setAnswers(Lists.newArrayList(answer, answer2))

    when(answerRepository.findOne(answer.getAnswerId)).thenReturn(answer)
    when(questionRepository.findOne(question.getQuestionId)).thenReturn(question)

    dut.deleteAnswer(loginUser, question.getQuestionId, answer.getAnswerId)
  }

  @Test(expected = classOf[AccessDeniedException])
  def deleteAnswer_differentUser {
    val loginUser: SocialUser = new SocialUser(10)
    val questionId: Long = 1L
    val answer: Answer = new Answer(2L)
    answer.writedBy(new SocialUser(11))
    when(answerRepository.findOne(answer.getAnswerId)).thenReturn(answer)
    dut.deleteAnswer(loginUser, questionId, answer.getAnswerId)
  }

  @Test
  def likeAnswer_ {
    val loginUser: SocialUser = new SocialUser(10)
    val answer: Answer = new Answer(2L)
    answer.writedBy(loginUser)
    when(answerRepository.findOne(answer.getAnswerId)).thenReturn(answer)
    dut.likeAnswer(loginUser, answer.getAnswerId)
  }
}
