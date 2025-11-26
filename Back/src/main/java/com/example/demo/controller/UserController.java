

// 	@GetMapping("/manager")
// 	public void manager() {
// 		log.info("GET /manager...");
// 	}
// 	@GetMapping("/admin")
// 	public void admin() {
// 		log.info("GET /admin...");
// 	}



// 	@GetMapping("/join")
// 	public void join() {
// 		log.info("GET /join..");
// 	}

// 	@PostMapping("/join")
// 	public String join_post(UserDto dto, RedirectAttributes redirectAttributes ) {
// 		log.info("POST /join.." + dto);

// 		//DTO->ENTITY(DB저장) , ENTITY->DTO(뷰로전달)
// 		dto.setPassword(  passwordEncoder.encode( dto.getPassword() ) );
// 		userRepository.save(dto.toEntity());

// 		boolean isJoin  = true;
// 		if(isJoin) {
// 			redirectAttributes.addFlashAttribute("message","회원가입 완료!");
// 			return "redirect:/login";
// 		}
// 		else
// 			return "join";
// 	}

// }


