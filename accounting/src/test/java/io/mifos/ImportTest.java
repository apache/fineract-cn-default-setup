/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mifos;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.fineract.cn.accounting.api.v1.client.LedgerManager;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.accounting.api.v1.domain.Ledger;
import org.apache.fineract.cn.accounting.importer.AccountImporter;
import org.apache.fineract.cn.accounting.importer.LedgerImporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
public class ImportTest {
  private Map<String, Ledger> createdLedgers = new HashMap<>();

  @MockBean
  LedgerManager ledgerManagerMock;

  @MockBean
  Logger loggerMock;

  @Test
  public void testStandardChartOfAccountsIsCorrectlyFormatted() throws IOException {
    final LedgerImporter ledgerImporter = new LedgerImporter(ledgerManagerMock, loggerMock);
    final URL ledgersUrl = ClassLoader.getSystemResource("standardChartOfAccounts/ledgers.csv");
    Assert.assertNotNull(ledgersUrl);
    ledgerImporter.importCSV(ledgersUrl);

    final AccountImporter accountImporter = new AccountImporter(ledgerManagerMock, loggerMock);
    final URL accountsUrl = ClassLoader.getSystemResource("standardChartOfAccounts/accounts.csv");
    Assert.assertNotNull(accountsUrl);
    accountImporter.importCSV(accountsUrl);
  }

  @Before
  public void prepare() {
    Mockito.doAnswer(new CollectCreatedLedgers(0)).when(ledgerManagerMock).createLedger(Matchers.any());
    Mockito.doAnswer(new CollectCreatedLedgers(1)).when(ledgerManagerMock).addSubLedger(Matchers.any(), Matchers.any());
    Mockito.doAnswer(new ReturnLedgers()).when(ledgerManagerMock).findLedger(Matchers.any());
    Mockito.doAnswer(new ValidateArgument<>(0, Account.class)).when(ledgerManagerMock).createAccount(Matchers.any());
  }

  class CollectCreatedLedgers implements Answer {
    private final int indexOfLedger;

    CollectCreatedLedgers(int indexOfLedger) {
      this.indexOfLedger = indexOfLedger;
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      final Ledger ledger = invocation.getArgumentAt(indexOfLedger, Ledger.class);

      final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
      Validator validator = factory.getValidator();
      final Set<ConstraintViolation<Ledger>> errors = validator.validate(ledger);
      if (errors.size() > 0)
        Assert.fail("Creation was requested for an invalid ledger.");

      createdLedgers.put(ledger.getIdentifier(), ledger);
      return null;
    }
  }

  class ReturnLedgers implements Answer {
    @Override
    public Ledger answer(InvocationOnMock invocation) throws Throwable {
      final String ledgerIdentifier = invocation.getArgumentAt(0, String.class);
      return createdLedgers.get(ledgerIdentifier);
    }
  }

  class ValidateArgument<T> implements Answer {
    private final int indexOfArgument;
    private final Class<T> thingyClass;

    ValidateArgument(final int indexOfArgument, final Class<T> thingyClass) {
      this.indexOfArgument = indexOfArgument;
      this.thingyClass = thingyClass;
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      final T thingy = invocation.getArgumentAt(indexOfArgument, thingyClass);

      final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
      Validator validator = factory.getValidator();
      final Set<ConstraintViolation<T>> errors = validator.validate(thingy);
      if (errors.size() > 0)
        Assert.fail("Creation was requested for an invalid thingy.");

      return null;
    }
  }
}
