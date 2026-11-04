/**
 * Arquitetura Front-end (Client-Side) em Angular + TypeScript + Tailwind CSS.
 * Implementa AuthGuard baseado em RBAC (bloqueando perfil RECEPCAO das rotas de Prontuário e Registro Documental)
 * e integração reativa com os endpoints RESTful do Spring Boot.
 */

export type PerfilUsuario = 'ADMIN' | 'PSICOLOGO' | 'RECEPCAO';

export interface SessaoJwt {
    token: string;
    login: string;
    perfil: PerfilUsuario;
    idPsicologoVinculado?: number;
}

export class ClinicaRbacAuthGuard {
    static podeAcessarProntuarios(sessao: SessaoJwt | null): boolean {
        if (!sessao || !sessao.token) {
            return false;
        }
        return sessao.perfil === 'PSICOLOGO' || sessao.perfil === 'ADMIN';
    }

    static podeAcessarRegistrosDocumentais(sessao: SessaoJwt | null): boolean {
        if (!sessao || !sessao.token) {
            return false;
        }
        return sessao.perfil === 'PSICOLOGO' && sessao.idPsicologoVinculado !== undefined;
    }

    static podeAcessarAgendaEDemografico(sessao: SessaoJwt | null): boolean {
        return sessao !== null && Boolean(sessao.token);
    }
}
