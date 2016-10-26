<?php 
	require_once('twitter-api/TwitterAPIExchange.php');
	 
$rpc_message = $_POST['rpc_message'];

$json = json_decode($rpc_message);
//recebe post do cliente informando a acao desejada
if(strcmp('getTwittes',$json->{'action'})===0){

//autenticacao para usar o Twitter api
		$authentication = array(
		 'oauth_access_token' => "770661023976198144-YjPFyWXlgSOVACvgYXTuV3W65tTmCLl",
		    'oauth_access_token_secret' => "VHC9KAYSCEEEjqCiohqEY18Gwkek5gGQVUM3UPvrJU700",
		    'consumer_key' => "r44Tmg3jqquie9Wjs1XCWPIBs",
		    'consumer_secret' => "68L8nWGp2mLoYTxsmydkdIhJ9gJi7g63a37AxbR32AWptnWqFX"
		);
	//REST action desejada
	$url = "https://api.twitter.com/1.1/statuses/user_timeline.json";
	//vetor com screen_name dos usuários que obteram 3 tweets de cada
	$users = array("alexandrekalil","98FC","em_com","realwbonner","Cruzeiro");
	 
	$requestMethod = "GET";
	$output = array();

	//para cada usuários irá gerar uma nova chamada get e obter os dados,gerando um Array de entradas(3 entradas para cada usuario)
	foreach ($users as $usr){
		$elem = array();
		$getfield = "?screen_name=".$usr."&count=3";
	 
		$twitter = new TwitterAPIExchange($authentication);
		$string = json_decode($twitter->setGetfield($getfield)
	             ->buildOauth($url, $requestMethod)
	             ->performRequest(),$assoc = TRUE);	
		foreach ($string as $val){	
			//para cada entrada gera um array com tempo de quando foi criado(em timestamp), nome do usário e texto
			$elem = array("time" => strtotime($val['created_at']), "autor" => $val['user']['name'], "texto" => $val['text']);
			//esse array gerado é inserido em formato JSON em outro array output
			array_push($output,json_encode($elem));
		}			
	}
	//array output é enviado ao client no formato JSON
	echo json_encode($output).'<br>';
	
}

?>