package com.promising.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.promising.repository.CommunityRepository;
import com.promising.repository.MemberRepository;
import com.promising.repository.PayRepository;
import com.promising.repository.ProjectRepository;

import com.promising.vo.CommunityVO;
import com.promising.vo.MemberVO;

import com.promising.vo.PageMaker;
import com.promising.vo.PageVO;
import com.promising.vo.PayVO;
import com.promising.vo.ProjectVO;

@Controller
@RequestMapping("/project")
public class ProjectController {
	Logger logger = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectRepository repo;
	@Autowired
	private MemberRepository memberrepo;
	@Autowired
	private CommunityRepository comrepo;
	@Autowired
	private PayRepository prepo;


	@GetMapping("/story/{pno}")
	public String projectStory(@PathVariable("pno") Long pno,Model model) {
		ProjectVO vo= repo.findById(pno).get();
		String profile = memberrepo.findByUname(vo.getPrWriter()).get().getSysName();
		
		model.addAttribute("vo",vo);
		model.addAttribute("profile",profile);

		return "project/story";
	}
	
	@GetMapping("/community/{pno}")
	public String projectCommunity(@PathVariable("pno") Long pno,Model model) {
	
		ProjectVO vo= repo.findById(pno).get();
		List<CommunityVO> comList = comrepo.getCommunities(vo);
		String profile = memberrepo.findByUname(vo.getPrWriter()).get().getSysName();
		
		model.addAttribute("vo",vo);
		model.addAttribute("com", comList);
		model.addAttribute("profile",profile);
		
		return "project/community";
	}
	
	@GetMapping("/notice/{pno}")
	public String projectNotice(@PathVariable("pno") Long pno,Model model) {

		ProjectVO vo= repo.findById(pno).get();
		String profile = memberrepo.findByUname(vo.getPrWriter()).get().getSysName();
		model.addAttribute("vo",vo);
		model.addAttribute("profile",profile);
		
		return "project/notice";

	}

	@PostMapping("/payment/{pno}")
	public String payment(@PathVariable("pno") Long pno, String amount, String price, Model model, Principal pcp) {
		
		MemberVO mvo = new MemberVO();
		mvo = memberrepo.findByUsername(pcp.getName()).get();
		ProjectVO vo= repo.findById(pno).get();
		
		model.addAttribute("amount",amount); 
		model.addAttribute("price",price);
		model.addAttribute("vo",vo); // 프로젝트 정보
		model.addAttribute("mvo",mvo); // 로그인 계정 정보
		model.addAttribute("pno",pno); 
		
		return "project/payment";
	}
	
	@RequestMapping("/completepay/{project}")
	public String paycomplete(@ModelAttribute("vo") PayVO vo, @PathVariable("project") Long pno) {

		prepo.save(vo);
		ProjectVO pp = repo.findById(pno).get();
		int price =  pp.getPrCurrentMoney() + Integer.parseInt(vo.getPrice());
		pp.setPrCurrentMoney(price);
		repo.save(pp);
		
		return "project/paycomplete";
	}
	
	@GetMapping("/main")
	public String main(Model model) {
		List<ProjectVO> result = repo.selectAll();
		model.addAttribute("result", result);
		return "project/main";
	}

	@GetMapping("/popular")
	public String popular(PageVO pvo, Model model) {
		Pageable page = pvo.makePageable(0, "pno");
		Page<ProjectVO> result = repo.selectPopular(repo.makePredicate(pvo.getType(), pvo.getKeyword()),page);
 		model.addAttribute("result", new PageMaker<ProjectVO>(result));
		return "project/list";
	}
	
	@GetMapping("/newest")
	public String newest(PageVO pvo, Model model) {
		Pageable page = pvo.makePageable(0, "pno");
		Page<ProjectVO> result = repo.selectNewest(repo.makePredicate(pvo.getType(), pvo.getKeyword()),page);
 		model.addAttribute("result", new PageMaker<ProjectVO>(result));
		return "project/list";
	}

	@GetMapping("/close")
	public String close(PageVO pvo, Model model) {
		Pageable page = pvo.makePageable(0, "pno");
		Page<ProjectVO> result = repo.selectClose(repo.makePredicate(pvo.getType(), pvo.getKeyword()),page);
 		model.addAttribute("result", new PageMaker<ProjectVO>(result));
		return "project/list";
	}

	@GetMapping("/list")
	public String list(PageVO pvo, Model model) {
		Pageable page = pvo.makePageable(0, "pno");
		Page<ProjectVO> result = repo.findAll(repo.makePredicate(pvo.getType(), pvo.getKeyword()),page);
		model.addAttribute("result", new PageMaker<ProjectVO>(result));
		return "project/list";
	}
	
	@GetMapping("/list2")
	public String list2(PageVO pvo, Model model) {
		Pageable page = pvo.makePageable(0, "pno");
		Page<ProjectVO> result = repo.findAll(repo.makePredicate2(pvo.getType(), Integer.parseInt(pvo.getKeyword()), Integer.parseInt(pvo.getKeyword2())),page);
		model.addAttribute("result", new PageMaker<ProjectVO>(result));
		return "project/list";
	}

	@GetMapping("/auth/upload1")
	public void upload1() {
		logger.debug("업로드하러옴1");
	}
	
	@GetMapping("/auth/upload2")
	public void upload2() {
		logger.debug("업로드하러옴22");
	}
	
	@GetMapping("/auth/upload3")
	public void upload3() {

	}

	@Transactional // 서비스로 옮길 예정
	@PostMapping("/auth/upload3")
	public String projectUpload(ProjectVO vo,MultipartFile[] file,Principal principal,String prStartday,String prEndday,String targetmoney,String presentprice) throws Exception {
		MemberVO newvo =memberrepo.findByUsername(principal.getName()).get();
		vo.setPrWriter(newvo.getUname());
		java.sql.Date prStartdate =java.sql.Date.valueOf(prStartday);
		java.sql.Date prEnddate =java.sql.Date.valueOf(prEndday);
		vo.setPrStartdate(prStartdate);
		vo.setPrEnddate(prEnddate);
		vo.setPrStatus("N");
		vo.setPrTargetMoney(Integer.parseInt(targetmoney));
		vo.setPrPresentPrice(Integer.parseInt(presentprice));

		File filesPath = new File("src"+File.separator+"main"+File.separator+"resources"+File.separator +"static"+File.separator+"images"+File.separator+"projectuploading");
		if(!filesPath.exists()) {
			filesPath.mkdir();
		}
		for(MultipartFile tmp :file) {
			if(tmp.getSize()>0) {
				String oriName = tmp.getOriginalFilename();
				String sysName=UUID.randomUUID().toString().replaceAll("-","")+"_"+oriName;
				vo.setPrOriName(oriName);
				vo.setPrSysName(sysName);
				tmp.transferTo(new File(filesPath.getAbsolutePath()+"/"+sysName));
			}
		}
		repo.save(vo);
		return "redirect:/project/complete";
	}
	
	@GetMapping("/complete")
	public void complete() {
	
	}
	@PostMapping("/summeruploading")
	@ResponseBody
	public String summerUploading(MultipartFile file) throws Exception {
		File filesPath = new File("src"+File.separator+"main"+File.separator+"resources"+File.separator +"static"+File.separator+"images"+File.separator+"summernoteuploading");
		if(!filesPath.exists()) {
			filesPath.mkdir();
		}
		String oriName = file.getOriginalFilename();
		String sysName=UUID.randomUUID().toString().replaceAll("-","")+"_"+oriName;
		file.transferTo(new File(filesPath.getAbsolutePath()+"/"+sysName));
	

		return sysName;

	}
}




