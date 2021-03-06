package br.com.terra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import br.com.terra.conexao.ConexaoMySQL;
import br.com.terra.configuracoes.Propriedades;

public class ConsultarMySQL implements Runnable {
	private int id;
	private static Properties propriedades = new Propriedades().getPropriedades();
	
	public static void main(String[] args) {
		for (int i = 0; i < Integer.valueOf(propriedades.getProperty("quantidade.concorrencia")); i++) {
			ConsultarMySQL inserir = new ConsultarMySQL();
			inserir.setId(i);
			Thread thread = new Thread(inserir);
			thread.start();
		}
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void run() {
		int contador = 0;
		Instant inicio = null;
		Instant termino = null;
		long mediaSegundos = 0;
		long mediaMilissegundos = 0;
		long mediaNanosegundos = 0;
		
		System.out.println("Início consultas MySQL");
		ConexaoMySQL conexaoMySQL = new ConexaoMySQL();
		Connection conexao = conexaoMySQL.getConexao();
		
		try {
			FileReader entrada = new FileReader(new File(propriedades.getProperty("caminho.arquivo")));
			BufferedReader leitura = new BufferedReader(entrada);
			String linha = "";
			
			while ((linha = leitura.readLine()) != null) {
				String[] lista = linha.split(",");
				String longitude = lista[2];
				String latitude = lista[3];
				
				String comando = String.format("SELECT latitude,longitude FROM coordenadas WHERE latitude=%s AND longitude=%s", latitude, longitude);
				
				inicio = Instant.now();
				PreparedStatement preparar = conexao.prepareStatement(comando);
				preparar.executeQuery();
				termino = Instant.now();
				
				Duration duracao = Duration.between(inicio, termino);
				mediaSegundos += duracao.getSeconds();
				mediaMilissegundos += duracao.toMillis();
				mediaNanosegundos += duracao.toNanos();
				++contador;
			}
			
			entrada.close();
			leitura.close();
			conexaoMySQL.fecharConexao();
			
			System.out.println("Término consultas");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		System.out.println("Thread: " + id);
		System.out.println("Dados consultados: " + contador);
		System.out.println("Média segundos: " + (mediaSegundos/contador));
		System.out.println("Média milissegundos: " + (mediaMilissegundos/contador));
		System.out.println("Média nanosegundos: " + (mediaNanosegundos/contador));
	}
}
