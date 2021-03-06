<?php

require_once('DAOController.php');
require_once('User.php');
require_once('UserView.php');
require_once('functionsnew.php');

class UserController extends DAOController
{
    protected $userSession, $adminSession;

    public function __construct($view, $conn, $allowedActions=null,
						$userSession, $adminSession, $table="users")
    {
		parent::__construct($view, $conn, $table, $allowedActions);	
        $this->userSession = $userSession;
        $this->adminSession = $adminSession;
    }

    public function login($username,$password,$redirect,$data)
    {
        $qs = "";
        $first=true;
        foreach($data as $k=>$v)
        {
			if($k!="action") 
			{
            	if($first==false)
                	$qs.="&";
            	else
                	$first=false;
            	$qs .= "$k=$v";
			}
        }
        $result = $this->doLogin($username,$password);
        if($result===false)
        {
            $this->view->redirectMsg("Invalid login",
                    basename($_SERVER['PHP_SELF']) . 
                    "?$qs&action=login&redirect=$redirect");
        }
        else
        {
			header("Location: $redirect?$qs");
        }
    }
    

    public  function doLogin($username,$password)
    {
		$this->dao->findUserByLogin($username,$password);
		$row = $this->dao->getRow();
        if($row!==false)
        {
            if($row["active"]!=-1)
            {
                $_SESSION[$this->userSession] = $row["username"];
                $_SESSION[$this->adminSession] = $row["isadmin"];
            }
        }
        return $row; 
    }

    public function remoteLogin($username,$password)
    {
        $result = $this->doLogin($username, $password);
        if($result===false)
            header("HTTP/1.1 401 Unauthorized");
        else
        {
            if($result["active"]==-1)
                header("HTTP/1.1 503 Service Unavailable");
            else
            {
                header("Content-type: application/json");
                $this->dao->findUserByUsername($_SESSION[$this->userSession]);
                $info = array ($_SESSION[$this->userSession],
                            $this->dao->isAdmin() ? "1":"0");
                echo json_encode($info);
            }
        }
    }
	
	public function actionGet($httpData) {
		$this->actionLogin($httpData);
	}

    public function actionLogin($httpData)
    {
        if(isset($httpData["username"]) && isset($httpData["password"]))
        {
            if(isset($httpData["remote"]) && $httpData["remote"])
                $this->remoteLogin 
                    ($httpData['username'],$httpData['password']);
            else
            {
                $username = $httpData["username"];
                $password = $httpData["password"];
                $redirect = isset($httpData["redirect"]) ? 
                    $httpData["redirect"]: "index.php";
                unset($httpData["username"]);
                unset($httpData["password"]);
                unset($httpData["redirect"]);
                $this->login($username, $password, $redirect, $httpData);
            }
        }
        else
        {
            //$this->view->head();
            $this->view->displayLogin
            (isset($httpData["redirect"]) ?  
                $httpData["redirect"] :"index.php", 
                "", $httpData); 
           //$this->view->closePage();
        }
    }

    public function actionLogout($httpData)
    {
        session_start();
        session_destroy();
        if(!isset($httpData['redirect']))
            $httpData['redirect'] = 'index.php';
        header("Location: $httpData[redirect]?action=login");

    }

    function actionSignup($httpData)
    {
        //$this->view->head();
        if(isset($httpData["username"]) && isset($httpData["password"]))
        {
            $res=$this->dao->processSignup
                ($httpData['username'],$httpData['password']);
            if(is_int($res))
                $this->view->displaySignupForm($res);
            else
                $this->view->displaySignupConfirmation();
        }
        else
        {
            $this->view->displaySignupForm();
        }
        //$this->view->closePage();
    }
}

?>
