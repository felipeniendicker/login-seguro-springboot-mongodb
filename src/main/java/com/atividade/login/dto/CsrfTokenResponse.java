package com.atividade.login.dto;

public record CsrfTokenResponse(
		String headerName,
		String parameterName,
		String token) {

	@Override
	public String toString() {
		return "CsrfTokenResponse[headerName=" + headerName
				+ ", parameterName=" + parameterName
				+ ", token=[PROTEGIDO]]";
	}
}
