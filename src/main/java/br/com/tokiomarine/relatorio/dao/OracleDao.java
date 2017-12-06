package br.com.tokiomarine.relatorio.dao;

import java.io.IOException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.Delegate;

/**
 * <p>Reponsavel pela conexao com o Oracle: as configuracoes de conexao estao no arquivo {@value "oracle/mybatis-config.xml"}.</p>
 */
public class OracleDao {

    Logger log = LoggerFactory.getLogger(OracleDao.class);

    // Arquivo de configuracao padrao do MyBatis.
    private static final String CONFIGURACAO = "oracle/mybatis-config.xml";  

    // Conexao do MyBatis com o Oracle.
    @Delegate
    private final SqlSession sqlSession;

    /**
     * <p>Cria o cliente com o Oracle atraves do {@code ambiente} informado.</p>
     *
     * @param ambiente ambiente (conexao) que sera utilizada
     *
     * @throws IOException erro na leitura do arquivo de configuracao do MyBatis
     */
    public OracleDao(final String ambiente) throws IOException {
        sqlSession = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream(CONFIGURACAO), ambiente).openSession();
    }

}