package com.panda.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.panda.domain.BoardVO;
import com.panda.domain.Criteria;
import com.panda.domain.MemberVO;
import com.panda.domain.PageVO;
import com.panda.service.BoardService;
import com.panda.service.MemberService;

import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

@Controller
@RequestMapping("/admin/*")
public class AdminController {
	
	@Inject
	private BoardService service;
	
	@Inject
	private MemberService mService;
	
	@Autowired
	private HttpSession session;
	
	private String resultURI="";
	
	// 로거객체
	private static final Logger mylog
		= LoggerFactory.getLogger(AdminController.class);
	
	
	// 어드민 메인페이지 
	// http://localhost:8080/admin/index
	@RequestMapping(value = "/index")
	public String mainGET()throws Exception{
		mylog.debug(" mainGET() 호출 ");
		String id = (String)session.getAttribute("user_id");
		resultURI = "";
		if(id == null || !id.equals("admin")) {
			
			return "redirect:/main/index";
		}
		
		resultURI = "/admin/index";
		
		return resultURI;
	}
	
	
	
	// 공지사항 게시판 작성페이지 GET
	// http://localhost:8080/admin/boardRegist
	@RequestMapping(value = "/boardRegist",method = RequestMethod.GET)
	public String boardRegistGET()throws Exception{
		mylog.debug(" boardRegistGET() 호출 ");
		String id = (String)session.getAttribute("user_id");
		resultURI = "";
		if(id == null || !id.equals("admin")) {
			
		 resultURI = "redirect:/main/index";
			
		}
		
		resultURI = "/admin/boardRegist";
		
		return resultURI;
	}
	
	
	// 공지사항 게시판 작성페이지 POST
	// http://localhost:8080/admin/boardRegist
	@RequestMapping(value = "/boardRegist",method = RequestMethod.POST)
	public String boardRegistPOST(BoardVO vo,RedirectAttributes rttr)throws Exception{
		mylog.debug(" boardRegistGET() 호출 ");
		String id = (String)session.getAttribute("user_id");
		if(id == null || !id.equals("admin")) {
			
			return "redirect:/main/index";
			
		}
		
		service.insertBoard(vo);
		
		mylog.debug(" 게시판 글쓰기 완료");
		
		// 글쓰기 완료시 전달.
		rttr.addFlashAttribute("result", "registOK");
		
		return "/admin/list";
	}
	
	// 공지사항 게시판 list GET (페이징처리)
	// http://localhost:8080/admin/list
	@RequestMapping(value = "/list",method = RequestMethod.GET)
	public String listGET(Criteria cri,Model model)throws Exception {
		
		
		mylog.debug(" listGET() 호출 ");
		
		String id = (String)session.getAttribute("user_id");
		resultURI = "";
		if(id == null || !id.equals("admin")) {
			
			resultURI = "redirect:/main/index";
			
		}
		
		
		
//		mylog.debug(" 글쓰기 결과 (result) : "+result);
		
		session.setAttribute("updateCheck", true);
		
		List<BoardVO> boardList =  service.getBoardAll(cri);
		
		// 페이징처리 하단부 정보 준비 -> view페이지 전달
		PageVO pvo = new PageVO();
		
		pvo.setCri(cri);
		
		
		mylog.debug(" totalCnt : "+service.totalCnt());
		pvo.setTotalCount(service.totalCnt()); // 작성되어있는 글 전체 개수
		
		
		model.addAttribute("pvo", pvo);
		model.addAttribute("boardList", boardList);
		
		resultURI = "/admin/list";
		
		return resultURI;
		
		}
	// 공지사항 게시판 list POST (페이징처리+카테고리)
	// http://localhost:8080/admin/list
	@RequestMapping(value = "/list",method = RequestMethod.POST)
	public String listPOST(HttpServletRequest request,
			Criteria cri,Model model)throws Exception {
		
		mylog.debug(" listPOST() 호출 ! "); 
		
		String id = (String)session.getAttribute("user_id");
		resultURI = "";
		if(id == null || !id.equals("admin")) {
			
			resultURI = "redirect:/main/index";
			
		}
		
		
		   String category = request.getParameter("category");
	       if(category.equals("all")) {
	    	   return "redirect:/admin/list";
	       }
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("cri", cri);
	        map.put("category", category);
		
	        // 카테고리 전달하는 리스트
	        List<BoardVO> boardList =  service.getBoardCate(map);
		
//		mylog.debug(" 글쓰기 결과 (result) : "+result);
		
		session.setAttribute("updateCheck", true);
		
		
		// 페이징처리 하단부 정보 준비 -> view페이지 전달
		PageVO pvo = new PageVO();
		
		pvo.setCri(cri);
		
		
		mylog.debug(" totalCnt : "+service.totalCnt());
		pvo.setTotalCount(service.totalCnt()); // 작성되어있는 글 전체 개수
		
		
		model.addAttribute("pvo", pvo);
		model.addAttribute("boardList", boardList);
		
		resultURI = "/admin/list";
		
		return resultURI;
		}
	
		
	// 게시판 본문보기 GET
	// http://localhost:8080/admin/content
	// http://localhost:8080/admin/content?bno=1
	@RequestMapping(value = "/content",method = RequestMethod.GET)
	public String contentGET(Model model,@RequestParam("bno") int bno,HttpSession session) throws Exception{
		mylog.debug(" contentGET() 호출 => 글번호(bno) : "+bno);
		
		String id = (String)session.getAttribute("user_id");
		resultURI = "";
		if(id == null || !id.equals("admin")) {
			
			resultURI = "redirect:/main/index";
			
		}
		
		
		boolean isUpdateCheck = (boolean)session.getAttribute("updateCheck");
		mylog.debug(" 조회수 증가 전 isUpdateCheck : "+isUpdateCheck);
		
		
		if(isUpdateCheck) {
			// list >> content 로 왔을때만 
			mylog.debug(" 조회수 1증가 ! ");
			
			
			session.setAttribute("updateCheck", !isUpdateCheck);
			
			mylog.debug(" isUpdateCheck : "+isUpdateCheck);
			
			service.upReadCnt(bno);
			
			
			
		}
		
		// 서비스 -> DAO (특정 글번호에 해당하는 정보 가져오기)
		BoardVO cvo = service.getContent(bno);
		
		model.addAttribute("cvo", cvo);
		
		resultURI = "/admin/content";
		
		
		return resultURI;
	}
	
	
	// 게시판 수정 GET
	@RequestMapping(value = "/modify",method = RequestMethod.GET)
	public String modifyGET(Model model,@ModelAttribute("bno") int bno)throws Exception{
		String id = (String)session.getAttribute("user_id");
		resultURI="";
		if(id == null || !id.equals("admin")) {
			
			resultURI = "redirect:/main/index";
			
		}
		model.addAttribute("cvo", service.getContent(bno));	
		
		resultURI = "/admin/modify";
		
		return resultURI;
	}
	
	// 게시판 수정 POST
	@RequestMapping(value = "/modify",method = RequestMethod.POST)
	public String modifyPOST(BoardVO vo,RedirectAttributes rttr)throws Exception{
		String id = (String)session.getAttribute("user_id");
		resultURI="";
		if(id == null || !id.equals("admin")) {
			
			resultURI = "redirect:/main/index";
			
		}
		
		mylog.debug(" modifyPOST() 호출"); 
		
		Integer result = service.updateBoard(vo);
		
		if(result>0) {
			// "수정완료" - 정보전달
			rttr.addFlashAttribute("result","modOK");
		}
		
		resultURI = "/admin/list";
		
		return resultURI;
	}
	
	// 게시판 삭제 POST
	@RequestMapping(value = "/remove",method = RequestMethod.POST)
	public String removePOST(@RequestParam("bno")int bno, RedirectAttributes rttr)throws Exception{
		

		Integer result = service.deleteBoard(bno);
		
		if(result>0) {
			// "수정완료" - 정보전달
			rttr.addFlashAttribute("result","remOK");
		}
		
		return "redirect:/admin/list";
	}
	
	// 회원목록 불러오기 GET
	// http://localhost:8080/admin/memberList
	@RequestMapping(value = "/memberList",method = RequestMethod.GET)
	public String mListGET(HttpSession session,Model model)throws Exception{
		mylog.debug(" mListGET()  호출 ");
		String id = (String)session.getAttribute("user_id");
		resultURI="";
		if(id == null || !id.equals("admin")) {
			
			resultURI = "redirect:/main/index";
			
		}
		
		List<MemberVO> memberList = mService.getMemList(id);
		
		model.addAttribute("memberList",memberList);
		
		mylog.debug(" 회원수 : "+memberList.size());
		
		resultURI = "/admin/memberList";
		
		return resultURI;
	}
	
	@RequestMapping(value = "/memberDetail", method = RequestMethod.GET)
	public void memberDetailGET()throws Exception{
		
		
		
	}
	
}
