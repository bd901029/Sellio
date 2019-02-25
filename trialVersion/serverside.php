/**
     * registerTrial
     */
    public function registerTrial($IMEINumber) {
        //check if $IMEINumber already exist
        // Instantiate DBH
        $DBH = new PDO_Wrapper();
        $DBH->query("SELECT date_reg FROM trials WHERE device_id = :IMEINumber");
        $DBH->bind(':IMEINumber', $IMEINumber);
        // DETERMINE HOW MANY ROWS OF RESULTS WE GOT
        $totalRows_registered = $DBH->rowCount();
        // DETERMINE HOW MANY ROWS OF RESULTS WE GOT
        $results = $DBH->resultset();

        if (!$IMEINumber) {
            return 'Device serial number could not be determined.';
        } else if ($totalRows_registered > 0) {
            $results = $results[0];
            $results = $results['date_reg'];
            return $results;
        } else {
            // Instantiate variables
            $trial_unique_id = es_generate_guid(60);
            $time_reg = date('H:i:s');
            $date_reg = date('Y-m-d');

            $DBH->beginTransaction();
            // opening db connection
            //NOW Insert INTO DB
            $DBH->query("INSERT INTO trials (time_reg, date_reg, date_time, device_id, trial_unique_id) VALUES (:time_reg, :date_reg, NOW(), :device_id, :trial_unique_id)");
            $arrayValue = array(':time_reg' => $time_reg, ':date_reg' => $date_reg, ':device_id' => $IMEINumber, ':trial_unique_id' => $trial_unique_id);
            $DBH->bindArray($arrayValue);
            $subscribe = $DBH->execute();
            $DBH->endTransaction();
            return $date_reg;
        }

    }